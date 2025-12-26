package bps.ipr.formulas

import bps.ipr.terms.Variable

object Falsity : FolFormula<Falsity> {
    override val variablesFreeIn: Set<Variable> = emptySet()

    override val symbol: String = "FALSITY"

    override fun display(): String =
        "(FALSITY)"

}
