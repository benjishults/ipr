package bps.ipr.prover.tableau.display

import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.listener.DisplayGoalsCompactListener
import bps.ipr.prover.tableau.listener.PopulateNodeWithFormulasListener
import bps.ipr.prover.tableau.listener.DisplayGoalsListener
import bps.ipr.prover.tableau.listener.DisplayHypsCompactListener
import bps.ipr.prover.tableau.listener.DisplayHypsListener
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
class DisplayableGraphvizTableauNodeHelper :
    PopulateNodeWithFormulasListener,
    DisplayHypsListener,
    DisplayGoalsListener,
    DisplayGoalsCompactListener,
DisplayHypsCompactListener{

    val nonAtomicGoals: MutableList<NegativeSignedFormula<*>> = mutableListOf()
    val nonAtomicHyps: MutableList<PositiveSignedFormula<*>> = mutableListOf()

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

    override fun displayHyps(builder: StringBuilder, indent: Int) {
        nonAtomicHyps.forEach {
            builder.appendLine(it.display(indent + 1))
        }
    }

    override fun displayGoals(builder: StringBuilder, indent: Int) {
        nonAtomicGoals.forEach {
            builder.appendLine(it.display(indent + 1))
        }
    }

    override fun displayHypsCompact(builder: StringBuilder, maxChars: Int) {
        nonAtomicHyps.forEach {
            builder.appendLine(it.display(maxChars))
        }
    }

    override fun displayGoalsCompact(builder: StringBuilder, maxChars: Int) {
        nonAtomicGoals.forEach {
            builder.appendLine(it.displayCompact(maxChars))
        }
    }

    companion object {
        fun addToNode(node: BaseTableauNode) {
            DisplayableGraphvizTableauNodeHelper()
                .also {
                    node.addPopulateListener(it)
                    node.addDisplayHypsListener(it)
                    node.addDisplayGoalsListener(it)
                    node.addDisplayHypsCompactListener(it)
                    node.addDisplayGoalsCompactListener(it)
                }
        }
    }

}
