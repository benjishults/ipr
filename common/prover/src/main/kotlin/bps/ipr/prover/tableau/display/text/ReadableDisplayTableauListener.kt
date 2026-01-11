package bps.ipr.prover.tableau.display.text

import bps.ipr.prover.tableau.BaseTableau
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.display.FlexibleDisplayTableauListener
import kotlin.text.appendLine

class ReadableDisplayTableauListener(
    val tableau: BaseTableau,
    override val nodeListenerFactory: (BaseTableauNode) -> ReadableDisplayNodeListener = ::ReadableDisplayNodeListener,
) : FlexibleDisplayTableauListener<ReadableDisplayNodeListener> {

    override fun displayTableau(appendable: Appendable) {
        tableau.root.preOrderTraverse { node: BaseTableauNode ->
            appendable.appendLine("---")
            appendable.append(node.displayNode("readable"))
        }
    }

}
