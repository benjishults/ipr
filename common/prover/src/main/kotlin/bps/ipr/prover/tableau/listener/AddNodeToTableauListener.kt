package bps.ipr.prover.tableau.listener

import bps.ipr.prover.tableau.BaseTableauNode

fun interface AddNodeToTableauListener {
    fun addNodeToTableau(node: BaseTableauNode)
}
