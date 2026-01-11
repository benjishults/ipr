package bps.ipr.prover.tableau.display.text

import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula

class ReadableDisplayNodeListener(
    node: BaseTableauNode,
) : bps.ipr.prover.tableau.display.DisplayNodeListener, bps.ipr.prover.tableau.display.FlexibleNodeDisplayHelper(node) {

    override fun displayNode(appendable: Appendable) {
        appendable.append(" ".repeat(node.depth))
        if (nonAtomicHyps.isNotEmpty() || node.newAtomicGoals.isNotEmpty()) {
            appendable.appendLine("(${node.id}) Suppose")
            node.newAtomicHyps.forEach { hyp: PositiveAtomicFormula ->
                appendable.appendLine(hyp.display(node.depth + 1))
            }
            displayNonAtomicHyps(appendable, node.depth)
            if (nonAtomicGoals.isNotEmpty() || node.newAtomicGoals.isNotEmpty()) {
                appendable.append(" ".repeat(node.depth))
                appendable.appendLine("Show")
            }
        } else {
            appendable.appendLine("(${node.id}) Show")
        }
        displayNonAtomicGoals(appendable, node.depth)
        node.newAtomicGoals.forEach { goal: NegativeAtomicFormula ->
            appendable.appendLine(goal.display(node.depth + 1))
        }
    }

    private fun displayNonAtomicHyps(appendable: Appendable, indent: Int) {
        nonAtomicHyps
            .forEach {
                appendable.appendLine(it.display(indent + 1))
            }
    }

    private fun displayNonAtomicGoals(appendable: Appendable, indent: Int) {
        nonAtomicGoals
            .forEach {
                appendable.appendLine(it.display(indent + 1))
            }
    }

}
