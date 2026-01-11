package bps.ipr.prover.tableau.closing

import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.FolTableauProofSuccess
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.Tableau

object SimplePreorderTableauClosingAlgorithm {

    fun Tableau<BaseTableauNode>.attemptCloseSimplePreorder(
        formulaUnifier: FormulaUnifier,
    ): FolProofSuccess? =
        with(SimplePreorderNodeClosingAlgorithm) {
            root
                .attemptCloseNode(
                    branchClosingSubstitution = null,
                    formulaUnifier = formulaUnifier,
                )
                .firstOrNull()
                ?.let { FolTableauProofSuccess(it.substitution, this@attemptCloseSimplePreorder) }
        }
}
