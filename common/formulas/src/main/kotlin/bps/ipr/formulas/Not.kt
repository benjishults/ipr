package bps.ipr.formulas

import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

class Not(val subFormula: FolFormula) : FolFormula() {
    //    override val variablesBoundIn: Set<BoundVariable> = subFormula.variablesBoundIn
    override val variablesFreeIn: Set<Variable> = subFormula.variablesFreeIn

    override val symbol: String = "NOT"

    override fun display(indent: Int): String =
        buildString {
            append(" ".repeat(indent))
            append("(NOT ${subFormula.display(0)})")
        }

    override fun apply(
        substitution: IdempotentSubstitution,
        formulaImplementation: FolFormulaImplementation,
    ) =
        if (substitution.domain.firstOrNull { it in this.variablesFreeIn } !== null)
            formulaImplementation.notOrNull(subFormula.apply(substitution, formulaImplementation))
        else
            this

}
