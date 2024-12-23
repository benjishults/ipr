package bps.ipr.formulas

import bps.ipr.terms.ArgumentList
import bps.ipr.terms.BoundVariable
import bps.ipr.terms.Substitution
import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation
import bps.ipr.terms.Variable
import bps.ipr.terms.FreeVariable

interface Formula {

    val symbol: String

    fun display(): String

}

sealed interface FolFormula : Formula {

    /**
     * Variables that occur free in this term.
     */
    val variablesFreeIn: Set<Variable>

//    /**
//     * Variables that are bound by variable-binding within this formula.
//     */
//    val variablesBoundIn: Set<BoundVariable>

    // TODO consider moving this to a subtype SyntacticTerm
    fun apply(substitution: Substitution, formulaImplementation: FolFormulaImplementation): FolFormula

}

class Predicate(
    override val symbol: String,
    val arguments: ArgumentList,
) : FolFormula {
    //    override val variablesBoundIn: Set<BoundVariable> = emptySet()
    override val variablesFreeIn: Set<Variable> =
        arguments
            .flatMapTo(mutableSetOf()) {
                it.variablesFreeIn
            }

    override fun apply(substitution: Substitution, formulaImplementation: FolFormulaImplementation): FolFormula =
        // short-circuit if we know the substitution won't disturb this term
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            TODO()
//            formulaImplementation.predicateOrNull(
//                symbol,
//                arguments
//                    .map {
//                        it.apply(substitution, formulaImplementation.termImplementation)
//                    },
//            )!!
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

class Not(val subFormula: FolFormula) : FolFormula {
    //    override val variablesBoundIn: Set<BoundVariable> = subFormula.variablesBoundIn
    override val variablesFreeIn: Set<Variable> = subFormula.variablesFreeIn

    override fun apply(substitution: Substitution, formulaImplementation: FolFormulaImplementation): Not =
        // short-circuit if we know the substitution won't disturb this term
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaImplementation.notOrNull(
                subFormula.apply(substitution, formulaImplementation),
            )
        else
            this

    override val symbol: String = "NOT"

    override fun display(): String =
        "(NOT ${subFormula.display()})"

}

abstract class AbstractMultiFolFormula<F : AbstractMultiFolFormula<F>>(
    vararg val subFormulas: FolFormula,
) : FolFormula {

    init {
        require(subFormulas.size >= 2)
    }

    abstract val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> F

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

    @Suppress("UNCHECKED_CAST")
    override fun apply(
        substitution: Substitution,
        formulaImplementation: FolFormulaImplementation,
    ): F =
        // short-circuit if we know the substitution won't disturb this term
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaConstructor(
                formulaImplementation,
                subFormulas
                    .mapTo(mutableListOf()) { it.apply(substitution, formulaImplementation) },
            )
        else
            this as F

    override fun display(): String =
        "(${subFormulas.joinToString(" $symbol ") { it.display() }})"

}

class And(vararg conjuncts: FolFormula) : AbstractMultiFolFormula<And>(*conjuncts) {
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> And =
        { impl, args ->
            impl.andOrNull(*args.toTypedArray())
        }

    override val symbol: String = "AND"
}

class Or(vararg disjuncts: FolFormula) : AbstractMultiFolFormula<Or>(*disjuncts) {
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> Or =
        { impl, args ->
            impl.orOrNull(*args.toTypedArray())
        }
    override val symbol: String = "OR"
}

class Equivalence(vararg subFormulas: FolFormula) : AbstractMultiFolFormula<Equivalence>(*subFormulas) {
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> Equivalence =
        { impl, args ->
            impl.iffOrNull(*args.toTypedArray())
        }
    override val symbol: String = "IFF"
}

class Implies(vararg subFormulas: FolFormula) : AbstractMultiFolFormula<Implies>(*subFormulas) {

    init {
        require(subFormulas.size == 2)
    }

    override val symbol: String = "IMPLIES"
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> Implies =
        { impl, args ->
            impl.impliesOrNull(args[0], args[1])
        }

    val antecedent: FolFormula = subFormulas[0]
    val consequent: FolFormula = subFormulas[1]

}

abstract class VariablesBindingFolFormula
/**
 * @throws IllegalArgumentException if any of the [BoundVariable]s in [boundVariables] display the same as any of
 * the [FreeVariable]s in [subFormula]'s [FolFormula.variablesFreeIn] OR if any of the [BoundVariable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    val boundVariables: List<BoundVariable>,
    val subFormula: FolFormula,
) : FolFormula {

    init {
        require(boundVariables.isNotEmpty())
        val freeVariablesInSubFormula =
            subFormula.variablesFreeIn
                .filterIsInstance<FreeVariable>()
                .map { it.display() }
        require(
            boundVariables
                .map { it.display() }
                .find { it in freeVariablesInSubFormula }
                    == null,
        )
        require(boundVariables.all { it in subFormula.variablesFreeIn })
    }

//    override val variablesBoundIn: Set<BoundVariable> =
//        subFormula.variablesBoundIn + boundVariables

    override val variablesFreeIn: Set<Variable> =
        boundVariables
            .map { it.display() }
            .let { boundVariableDisplays ->
                subFormula.variablesFreeIn -
                        subFormula.variablesFreeIn
                            .filterIsInstance<BoundVariable>()
                            .filter { it.display() in boundVariableDisplays }
                            .toSet()
            }

    override fun apply(substitution: Substitution, formulaImplementation: FolFormulaImplementation): FolFormula {
        TODO("Not yet implemented")
    }

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
    boundVariables: List<BoundVariable>,
    subFormula: FolFormula,
) : VariablesBindingFolFormula(boundVariables, subFormula) {

    override val symbol: String =
        "FORALL"

}

class ForSome
/**
 * @throws IllegalArgumentException if any of the [BoundVariable]s in [boundVariables] display the same as any of
 * the [FreeVariable]s in [subFormula]'s [FolFormula.variablesFreeIn] OR if any of the [BoundVariable]s in
 * [boundVariables] do NOT occur free in [subFormula].
 */
constructor(
    boundVariables: List<BoundVariable>,
    subFormula: FolFormula,
) : VariablesBindingFolFormula(boundVariables, subFormula) {

    override val symbol: String =
        "FORSOME"

}
