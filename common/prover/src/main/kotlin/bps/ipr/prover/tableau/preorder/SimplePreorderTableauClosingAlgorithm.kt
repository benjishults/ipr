package bps.ipr.prover.tableau.preorder

import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.FolTableauProofSuccess
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.Tableau
import bps.ipr.prover.tableau.preorder.SimplePreorderNodeClosingAlgorithm.attemptCloseNode

object SimplePreorderTableauClosingAlgorithm {

    fun Tableau<BaseTableauNode>.attemptCloseSimplePreorder(
        formulaUnifier: FormulaUnifier,
    ): FolProofSuccess? =
        root
            .attemptCloseNode(
                substitution = null,
                positiveAtomsAbove = emptyList(),
                negativeAtomsAbove = emptyList(),
                formulaUnifier = formulaUnifier,
            )
            .firstOrNull()
            ?.let { FolTableauProofSuccess(it, this) }
}
