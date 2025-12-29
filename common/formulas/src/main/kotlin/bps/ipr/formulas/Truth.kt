package bps.ipr.formulas

import bps.ipr.terms.Variable

object Truth : FolFormula {
    override val variablesFreeIn: Set<Variable> = emptySet()

    override val symbol: String = "TRUTH"

    override fun display(): String =
        "(TRUTH)"

}
