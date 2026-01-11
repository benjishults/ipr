package bps.ipr.prover.tableau.display.dot

import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.display.DisplayNodeListener
import bps.ipr.prover.tableau.display.FlexibleNodeDisplayHelper
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula

class DotDisplayNodeListener(
    node: BaseTableauNode,
) : DisplayNodeListener, FlexibleNodeDisplayHelper(node) {

    override fun displayNode(appendable: Appendable) {
        appendable.appendLine(
            """
                |"${node.id}" [
                |label="${displayCompact()}"
                |]"""
                .trimMargin(),
        )
    }

    fun displayCompact(): String =
        buildString {
            if (node.newAtomicHyps.isNotEmpty() || nonAtomicHyps.isNotEmpty()) {
                append("Suppose\\l")
                node.newAtomicHyps.forEach { hyp: PositiveAtomicFormula ->
                    append(hyp.displayCompact())
                }
                nonAtomicHyps.forEach { hyp ->
                    append(hyp.displayCompact())
                }
            }
            if (node.newAtomicGoals.isNotEmpty() || nonAtomicGoals.isNotEmpty()) {
                append("Show\\l")
                node.newAtomicGoals.forEach { goal: NegativeAtomicFormula ->
                    append(goal.displayCompact())
                }
                nonAtomicGoals.forEach { goal ->
                    append(goal.displayCompact())
                }
            }
        }

}
