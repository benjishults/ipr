package bps.ipr.formulas

import bps.ipr.terms.ArgumentList
import bps.ipr.terms.FreeVariable
import bps.ipr.terms.Substitution
import bps.ipr.terms.Variable

interface Formula {

    val symbol: String

    fun display(): String

}

sealed interface FolFormula<out F : FolFormula<F>> : Formula {

    /**
     * Variables that occur free in this term.
     */
    val variablesFreeIn: Set<Variable>

//    /**
//     * Variables that are bound by variable-binding within this formula.
//     */
//    val variablesBoundIn: Set<BoundVariable>

}

class Predicate(
    override val symbol: String,
    val arguments: ArgumentList,
) : FolFormula<Predicate> {
    //    override val variablesBoundIn: Set<BoundVariable> = emptySet()
    override val variablesFreeIn: Set<Variable> =
        arguments
            .flatMapTo(mutableSetOf()) {
                it.variablesFreeIn
            }

    fun apply(substitution: Substitution, formulaImplementation: FolFormulaImplementation): Predicate =
        // short-circuit if we know the substitution won't disturb this term
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaImplementation.predicateOrNull(
                symbol,
                arguments
                    .map {
                        it.apply(substitution, formulaImplementation.termImplementation)
                    },
            )!!
        else
            this

    override fun display(): String {
        return "$symbol${
            arguments
                .map { it.display() }
                .joinToString(", ", "(", ")") { it }
        }"
    }

}

class Not(val subFormula: FolFormula<*>) : FolFormula<Not> {
    //    override val variablesBoundIn: Set<BoundVariable> = subFormula.variablesBoundIn
    override val variablesFreeIn: Set<Variable> = subFormula.variablesFreeIn

    override val symbol: String = "NOT"

    override fun display(): String =
        "(NOT ${subFormula.display()})"

}

abstract class AbstractMultiFolFormula<out F : AbstractMultiFolFormula<F>>(
    vararg val subFormulas: FolFormula<*>,
) : FolFormula<F> {

    init {
        require(subFormulas.size >= 2)
    }

    abstract val orderMattersLogically: Boolean

    abstract val formulaConstructor: (FolFormulaImplementation, List<FolFormula<*>>) -> F

//    override val variablesBoundIn: Set<BoundVariable> =
//        subFormulas
//            .flatMapTo(mutableSetOf()) {
//                it.variablesBoundIn
//            }

    override val variablesFreeIn: Set<Variable> =
        subFormulas
            .flatMapTo(mutableSetOf()) {
                it.variablesFreeIn
            }

    override fun display(): String =
        "(${subFormulas.joinToString(" $symbol ") { it.display() }})"

}

class And(vararg conjuncts: FolFormula<*>) : AbstractMultiFolFormula<And>(*conjuncts) {
    override val orderMattersLogically: Boolean = false
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula<*>>) -> And =
        { impl, args ->
            impl.andOrNull(*args.toTypedArray())
        }

    override val symbol: String = "AND"
}

class Or(vararg disjuncts: FolFormula<*>) : AbstractMultiFolFormula<Or>(*disjuncts) {
    override val orderMattersLogically: Boolean = false
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula<*>>) -> Or =
        { impl, args ->
            impl.orOrNull(*args.toTypedArray())
        }
    override val symbol: String = "OR"
}

class Equivalence(vararg subFormulas: FolFormula<*>) : AbstractMultiFolFormula<Equivalence>(*subFormulas) {
    override val orderMattersLogically: Boolean = false
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula<*>>) -> Equivalence =
        { impl, args ->
            impl.iffOrNull(*args.toTypedArray())
        }
    override val symbol: String = "IFF"
}

class Implies(vararg subFormulas: FolFormula<*>) : AbstractMultiFolFormula<Implies>(*subFormulas) {

    override val orderMattersLogically: Boolean = true

    init {
        require(subFormulas.size == 2)
    }

    override val symbol: String = "IMPLIES"
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula<*>>) -> Implies =
        { impl, args ->
            impl.impliesOrNull(args[0], args[1])
        }

    val antecedent: FolFormula<*> = subFormulas[0]
    val consequent: FolFormula<*> = subFormulas[1]

}

abstract class VariablesBindingFolFormula<out F : VariablesBindingFolFormula<F>>
/**
 * @throws IllegalArgumentException if [boundVariables] is empty or any of the [Variable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    val boundVariables: List<Variable>,
    val subFormula: FolFormula<*>,
) : FolFormula<F> {

    init {
        require(boundVariables.isNotEmpty())
//        val freeVariablesInSubFormula =
//            subFormula.variablesFreeIn
//                .filterIsInstance<FreeVariable>()
//                .map { it.display() }
//        require(
//            boundVariables
//                .map { it.display() }
//                .find { it in freeVariablesInSubFormula }
//                    == null,
//        )
        require(boundVariables.all { it in subFormula.variablesFreeIn })
    }

//    override val variablesBoundIn: Set<BoundVariable> =
//        subFormula.variablesBoundIn + boundVariables

    abstract val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula<*>) -> F

    override val variablesFreeIn: Set<Variable> =
//        boundVariables
//            .map { it.display() }
//            .let { boundVariableDisplays ->
        subFormula.variablesFreeIn -
                boundVariables
//                            .filterIsInstance<BoundVariable>()
//                            .filter { it.display() in boundVariableDisplays }
                    .toSet()
//            }

    override fun display(): String =
        buildString {
            append(
                boundVariables
                    .map { it.display() }
                    .joinToString(", ", "($symbol (", ") "),
            )
            append(subFormula.display())
            append(")")
        }
}

class ForAll
/**
 * @throws IllegalArgumentException if any of the [BoundVariable]s in [boundVariables] display the same as any of
 * the [FreeVariable]s in [subFormula]'s [FolFormula.variablesFreeIn] OR if any of the [BoundVariable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    boundVariables: List<Variable>,
    subFormula: FolFormula<*>,
) : VariablesBindingFolFormula<ForAll>(boundVariables, subFormula) {

    override val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula<*>) -> ForAll =
        { folFormulaImplementation, boundVariables, folFormula ->
            folFormulaImplementation.forAllOrNull(boundVariables, folFormula)
        }

    override val symbol: String =
        "FORALL"

}

class ForSome
/**
 * @throws IllegalArgumentException if any of the [Variable]s in [boundVariables] display the same as any of
 * the [FreeVariable]s in [subFormula]'s [FolFormula.variablesFreeIn] OR if any of the [Variable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    boundVariables: List<Variable>,
    subFormula: FolFormula<*>,
) : VariablesBindingFolFormula<ForSome>(boundVariables, subFormula) {

    override val formulaConstructor: (FolFormulaImplementation, List<Variable>, FolFormula<*>) -> ForSome =
        { folFormulaImplementation, boundVariables, folFormula ->
            folFormulaImplementation.forSomeOrNull(boundVariables, folFormula)
        }

    override val symbol: String =
        "FORSOME"

}
