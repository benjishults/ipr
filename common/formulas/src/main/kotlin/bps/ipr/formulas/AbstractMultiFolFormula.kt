package bps.ipr.formulas

import bps.ipr.terms.Variable

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
