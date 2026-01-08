package bps.ipr.prover.tableau

import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.tableau.rule.RuleSelector

interface Tableau<N : TableauNode<N>> {
    val root: N
    val applicableRules: RuleSelector
    fun attemptClose(formulaUnifier: FormulaUnifier): FolProofSuccess?
    fun display(): String
}
