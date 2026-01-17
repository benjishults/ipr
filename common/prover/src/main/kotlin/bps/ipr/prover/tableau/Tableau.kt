package bps.ipr.prover.tableau

import bps.ipr.prover.tableau.closing.CondensingBranchCloser
import bps.ipr.prover.tableau.rule.RuleSelector

// TODO eventually, I'll have more specific types for non-FOL and non-condensing possibilities
interface Tableau<N : TableauNode<N>, C: CondensingBranchCloser> {
    val root: N
    val applicableRules: RuleSelector
    fun display(appendable: Appendable, displayKey: String = "plain")
}
