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

}


