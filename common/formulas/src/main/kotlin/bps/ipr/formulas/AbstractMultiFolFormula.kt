package bps.ipr.formulas

import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

sealed class AbstractMultiFolFormula(
    val subFormulas: List<FolFormula>,
) : FolFormula() {

    init {
        require(subFormulas.size >= 2)
    }

    abstract val orderMattersLogically: Boolean

    abstract val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> AbstractMultiFolFormula

//    override val variablesBoundIn: Set<BoundVariable> =
//        subFormulas
//            .flatMapTo(mutableSetOf()) {
//                it.variablesBoundIn
//            }

    override fun apply(
        substitution: IdempotentSubstitution,
        formulaImplementation: FolFormulaImplementation,
    ): AbstractMultiFolFormula =
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaConstructor(formulaImplementation, subFormulas.map { it.apply(substitution, formulaImplementation) })
        else
            this

    override val variablesFreeIn: Set<Variable> =
        subFormulas
            .flatMapTo(mutableSetOf()) {
                it.variablesFreeIn
            }

    override fun display(indent: Int): String =
        buildString {
            append(" ".repeat(indent))
            append(
                subFormulas.joinToString(
                    separator = " ",
                    prefix = "($symbol ",
                    postfix = ")",
                ) { it.display() },
            )
        }

}

class And(conjuncts: List<FolFormula>) : AbstractMultiFolFormula(conjuncts) {
    override val orderMattersLogically: Boolean = false
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> And =
        { impl: FolFormulaImplementation, args: List<FolFormula> ->
            impl.andOrNull(args)
        }

    override val symbol: String = "AND"
}

class Or(disjuncts: List<FolFormula>) : AbstractMultiFolFormula(disjuncts) {
    override val orderMattersLogically: Boolean = false
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> Or =
        { impl: FolFormulaImplementation, args: List<FolFormula> ->
            impl.orOrNull(args)
        }
    override val symbol: String = "OR"
}

class Iff(subFormulas: List<FolFormula>) : AbstractMultiFolFormula(subFormulas) {
    override val orderMattersLogically: Boolean = false

    init {
        // NOTE might loosen this later
        require(subFormulas.size == 2)
    }

    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> Iff =
        { impl: FolFormulaImplementation, args: List<FolFormula> ->
            impl.iffOrNull(args)
        }
    override val symbol: String = "IFF"
}

class Implies(subFormulas: List<FolFormula>) : AbstractMultiFolFormula(subFormulas) {

    override val orderMattersLogically: Boolean = true

    init {
        require(subFormulas.size == 2)
    }

    override val symbol: String = "IMPLIES"
    override val formulaConstructor: (FolFormulaImplementation, List<FolFormula>) -> Implies =
        { impl: FolFormulaImplementation, args: List<FolFormula> ->
            impl.impliesOrNull(args)
        }

    val antecedent: FolFormula = subFormulas[0]
    val consequent: FolFormula = subFormulas[1]

}
