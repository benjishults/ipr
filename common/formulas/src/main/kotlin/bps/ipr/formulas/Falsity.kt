package bps.ipr.formulas

import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

object Falsity : FolFormula() {
    override val variablesFreeIn: Set<Variable> = emptySet()

    override fun apply(
        substitution: IdempotentSubstitution,
        formulaImplementation: FolFormulaImplementation,
    ) =
        this

    override val symbol: String = "FALSITY"

    override fun display(indent: Int): String =
        buildString {
            append(" ".repeat(indent))
            append("(FALSITY)")
        }

}
