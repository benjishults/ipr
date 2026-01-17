package bps.ipr.prover.tableau

import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula

interface TableauNode<T: TableauNode<T>> {
    val tableau: Tableau<T, *>
    val children: List<T>
    val newAtomicHyps: List<PositiveAtomicFormula>
    val newAtomicGoals: List<NegativeAtomicFormula>
    val closables: List<ClosingFormula<*>>
}
