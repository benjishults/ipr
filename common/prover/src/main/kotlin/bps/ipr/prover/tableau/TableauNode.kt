package bps.ipr.prover.tableau

import bps.ipr.common.Queue

class TableauNode(
    val tableau: Tableau,
    var parent: TableauNode? = null,
    var newHyps: List<SignedFormula<*>> = emptyList(),
    var newGoals: List<SignedFormula<*>> = emptyList(),
    var children: List<TableauNode> = emptyList(),
) {

    fun findLeaves(): List<TableauNode> =
        // NOTE a depth-first implementation with flatmap creates a lot of garbage
        buildList {
            breadthFirstTraverse {
                if (it.children.isEmpty())
                    add(it)
            }
        }

    fun createChildNodes(): List<TableauNode> =
        children
            .takeIf { it.isNotEmpty() }
            ?.flatMap { it.createChildNodes() }
            ?: listOf(TableauNode(tableau, this))
                .also { children = it }

    fun breadthFirstTraverse(operation: (TableauNode) -> Unit) =
        Queue<TableauNode>()
            .apply { enqueue(this@TableauNode) }
            .let { queue ->
                var next = queue.dequeue()
                while (next !== null) {
                    operation(next)
                    next.children.forEach { queue.enqueue(it) }
                }
            }

}
