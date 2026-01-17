package bps.ipr.prover.tableau.display.dot

import bps.ipr.prover.tableau.BaseTableau
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.display.FlexibleDisplayTableauListener

class DotDisplayTableauListener(
    val tableau: BaseTableau<*>,
    override val nodeListenerFactory: (BaseTableauNode) -> DotDisplayNodeListener = ::DotDisplayNodeListener,
) : FlexibleDisplayTableauListener<DotDisplayNodeListener> {

    override fun displayTableau(appendable: Appendable) {
        appendable.appendLine("digraph G {")
        appendable.appendLine("layout=dot")
        appendable.appendLine("""
            |root="0"
            """.trimMargin())
        appendable.appendLine("node [shape=box]")
        tableau
            .root
            .breadthFirstTraverse { node: BaseTableauNode ->
                appendable.append(node.displayNode("dot"))
                if (node.parent != null)
                    appendable.appendLine(""""${node.parent!!.id}" -> "${node.id}"""")
            }
        appendable.appendLine("}")
    }

}
