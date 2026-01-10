package bps.ipr.prover.tableau.display

import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.listener.AddNodeToTableauListener

/**
 * Adding this to a [bps.ipr.prover.tableau.BaseTableau] will dynamically make each additional node displayable.
 */
object DisplayingAddNodeToTableauListener : AddNodeToTableauListener {

    override fun addNodeToTableau(node: BaseTableauNode) {
        DisplayableGraphvizTableauNodeHelper.addToNode(node)
    }

}
