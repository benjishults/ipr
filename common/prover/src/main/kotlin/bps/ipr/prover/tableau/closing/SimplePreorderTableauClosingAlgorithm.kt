package bps.ipr.prover.tableau.closing

import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofResult
import bps.ipr.prover.FolTableauProofSuccess
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.Tableau

object SimplePreorderTableauClosingAlgorithm {

    fun <C> Tableau<BaseTableauNode, C>.attemptCloseSimplePreorder(
        formulaUnifier: FormulaUnifier,
        branchCloserExtender: BranchCloserExtender<C>,
    ): FolProofResult<C>? where C : FolBranchCloser, C: CondensingBranchCloser =
        with(SimplePreorderNodeClosingAlgorithm) {
            root
                .attemptCloseNode(
                    branchClosingSubstitution = null,
                    formulaUnifier = formulaUnifier,
                    branchCloserExtender = branchCloserExtender,
                )
                .firstOrNull()
                ?.let { FolTableauProofSuccess(it, this@attemptCloseSimplePreorder) }
        }
}
