package bps.ipr.formulas

import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

object Truth : FolFormula() {
    override val variablesFreeIn: Set<Variable> = emptySet()

    override val symbol: String = "TRUTH"

    override fun apply(
        substitution: IdempotentSubstitution,
        formulaImplementation: FolFormulaImplementation,
    ) =
        this

    override fun display(indent: Int): String =
        buildString {
            append(" ".repeat(indent))
            append("(TRUTH)")
        }

}
