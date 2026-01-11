package bps.ipr.prover.tableau.display

import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.listener.AddNodeToTableauListener
import bps.ipr.prover.tableau.listener.PopulateNodeWithFormulasListener

/**
 * Adding an instance to a [bps.ipr.prover.tableau.BaseTableau] will dynamically make each additional node displayable.
 */
interface FlexibleDisplayTableauListener<N> :
    AddNodeToTableauListener,
    DisplayTableauListener
        where N : DisplayNodeListener,
              N : PopulateNodeWithFormulasListener {

    val nodeListenerFactory: (BaseTableauNode) -> N

    override fun addNodeToTableau(node: BaseTableauNode) {
        nodeListenerFactory(node)
            .also {
                node.addPopulateListener(it)
                node.addDisplayNodeListener(it)
            }
    }

}
