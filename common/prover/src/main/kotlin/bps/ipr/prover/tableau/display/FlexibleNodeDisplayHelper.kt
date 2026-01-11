package bps.ipr.prover.tableau.display

import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.listener.PopulateNodeWithFormulasListener
import bps.ipr.prover.tableau.rule.BetaFormula
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.DeltaFormula
import bps.ipr.prover.tableau.rule.GammaFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.NegativeSignedFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveSignedFormula
import bps.ipr.prover.tableau.rule.SignedFormula

/**
 * An instance of this should be associated to a single [bps.ipr.prover.tableau.BaseTableauNode] in order to make
 * it displayable.
 */
// FIXME make this a PopulateNodeWithFormulasListener that can have multiple DisplayNodeListeners within it
abstract class FlexibleNodeDisplayHelper(
    val node: BaseTableauNode,
) : PopulateNodeWithFormulasListener {

    protected val nonAtomicGoals: MutableList<NegativeSignedFormula<*>> = mutableListOf()
    protected val nonAtomicHyps: MutableList<PositiveSignedFormula<*>> = mutableListOf()

    private fun <T : SignedFormula<*>> distributor(formula: T) {
        when (formula) {
            is NegativeSignedFormula<*> -> nonAtomicGoals.add(formula)
            is PositiveSignedFormula<*> -> nonAtomicHyps.add(formula)
        }
    }

    override fun populateNodeWithFormulas(
        newAtomicHyps: List<PositiveAtomicFormula>?,
        newAtomicGoals: List<NegativeAtomicFormula>?,
        closing: List<ClosingFormula<*>>?,
        betas: List<BetaFormula<*>>?,
        deltas: List<DeltaFormula<*>>?,
        gammas: List<GammaFormula<*>>?,
    ) {
        closing
            ?.forEach(::distributor)
        betas
            ?.forEach(::distributor)
        deltas
            ?.forEach(::distributor)
        gammas
            ?.forEach(::distributor)
    }

//    override fun displayHyps(appendable: Appendable, indent: Int) {
//        nonAtomicHyps.forEach {
//            appendable.appendLine(it.display(indent + 1))
//        }
//    }
//
//    override fun displayGoals(appendable: Appendable, indent: Int) {
//        nonAtomicGoals.forEach {
//            appendable.appendLine(it.display(indent + 1))
//        }
//    }
//
//    override fun displayHypsCompact(appendable: Appendable, maxChars: Int) {
//        nonAtomicHyps.forEach {
//            appendable.append(it.displayCompact(maxChars))
//        }
//    }
//
//    override fun displayGoalsCompact(appendable: Appendable, maxChars: Int) {
//        nonAtomicGoals.forEach {
//            appendable.append(it.displayCompact(maxChars))
//        }
//    }

//    override fun displayNode(appendable: Appendable, indent: Int) {
//        buildString {
//            append(" ".repeat(indent))
//            appendLine("(${node.id}) Suppose")
//            node.newAtomicHyps.forEach { hyp: PositiveAtomicFormula ->
//                appendLine(hyp.display(indent + 1))
//            }
//            displayHyps(this, indent)
//            append(" ".repeat(indent))
//            appendLine("Show")
//            node.newAtomicGoals.forEach { goal: NegativeAtomicFormula ->
//                appendLine(goal.display(indent + 1))
//            }
//            displayGoals(this, indent)
//        }
//    }

//    companion object {
//        /**
//         * You don't want an instance of this class to be associated with more than one node.  Calling this method
//         * will give you a new instance that adds itself as a listener to the given node.
//         */
//        fun addToNode(node: BaseTableauNode) {
//            FlexibleNodeDisplayHelper(node)
//                .also {
//                    node.addPopulateListener(it)
//                    node.addDisplayHypsListener(it)
//                    node.addDisplayGoalsListener(it)
//                    node.addDisplayHypsCompactListener(it)
//                    node.addDisplayGoalsCompactListener(it)
//                }
//        }
//    }

}


