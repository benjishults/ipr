package bps.ipr.prover.tableau

import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula

interface TableauNode {
    val tableau: Tableau<TableauNode>
    val children: List<TableauNode>
    val newAtomicHyps: List<PositiveAtomicFormula>
    val newAtomicGoals: List<NegativeAtomicFormula>
    val closables: List<ClosingFormula<*>>
}
