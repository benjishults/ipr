package bps.ipr.prover.tableau

import bps.ipr.common.Queue
import bps.ipr.common.queue

/**
 * This class is not thread-safe.
 */
class TableauNode(
    parent: TableauNode? = null,
    newAtomicHyps: List<PositiveAtomicFormula>? = null,
    newAtomicGoals: List<NegativeAtomicFormula>? = null,
) {

    private var _tableau: Tableau? = null

    /**
     * Setting this registers the node in the tableau which includes incrementing the size of the tableau and
     * setting the [id] of the node.
     */
    var tableau: Tableau
        get() = _tableau!!
        set(value) {
            if (_tableau === null) {
                _tableau = value
                // NOTE no need to register the node here because this is called by registerNode
            } else
                throw IllegalStateException("Tableau already set")
        }

    private var _id: Long? = null
    var id: Long
        get() = _id!!
        set(value) {
            if (_id === null)
                _id = value
            else
                throw IllegalStateException("Id already set")
        }

    private var _parent: TableauNode? = parent
    val parent: TableauNode?
        get() = _parent

    private var _children: List<TableauNode> = emptyList()
    val children: List<TableauNode>
        get() = _children

    private var _newAtomicHyps: List<PositiveAtomicFormula>? = newAtomicHyps
    val newAtomicHyps: List<PositiveAtomicFormula>
        get() = _newAtomicHyps!!

    private var _newAtomicGoals: List<NegativeAtomicFormula>? = newAtomicGoals
    val newAtomicGoals: List<NegativeAtomicFormula>
        get() = _newAtomicGoals!!

    private var _closables: List<ClosingFormula<*>>? = null
    val closables: List<ClosingFormula<*>>
        get() = _closables!!

    fun populate(
        newAtomicHyps: List<PositiveAtomicFormula>,
        newAtomicGoals: List<NegativeAtomicFormula>,
        closing: List<ClosingFormula<*>>,
        betas: List<BetaFormula<*>>,
        deltas: List<DeltaFormula<*>>,
        gammas: List<GammaFormula<*>>,
    ) {
        if (_newAtomicHyps === null && _newAtomicGoals === null) {
            _newAtomicHyps = newAtomicHyps
            _newAtomicGoals = newAtomicGoals
            _closables = closing
            betas.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            deltas.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            gammas.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            closing.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
        } else {
            throw IllegalStateException("New hyps and new goals already set")
        }
    }

    fun leaves(): List<TableauNode> =
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
            ?: listOf(TableauNode(parent = this))
                .also { _children = it }

    fun depth(): Int =
        parent
            ?.let { 1 + it.depth() }
            ?: 0

    fun <T : Any> preOrderAccumulateByBranch(accumulator: T, operation: TableauNode.(T) -> T?): Boolean =
        operation(accumulator)
            ?.let { accumulatedValue: T ->
                children
                    .all {
                        it.preOrderAccumulateByBranch(accumulatedValue, operation)
                    }
            }
            ?: false

    fun <T : Any> postOrderTraverse(operation: (TableauNode) -> T?): T? {
        children.forEach {
            val result: T? = it.postOrderTraverse(operation)
            if (result === null)
                return null
        }
        return operation(this)
    }

    fun breadthFirstTraverse(operation: (TableauNode) -> Unit) =
        queue<TableauNode>()
            .apply { enqueue(this@TableauNode) }
            .let { queue: Queue<TableauNode> ->
                while (true) {
                    val next: TableauNode? = queue.dequeueOrNull()
                    if (next !== null) {
                        operation(next)
                        next
                            .children
                            .forEach { queue.enqueue(it) }
                    } else
                        break
                }
            }

    fun setChildren(newChildrenList: List<TableauNode>) {
        if (_children.isEmpty()) {
            _children = newChildrenList
            newChildrenList.forEach { newChildNode: TableauNode ->
                newChildNode._parent = this@TableauNode
            }
        } else {
            throw IllegalStateException("Children already set")
        }
    }

    fun display(indent: Int) =
        buildString {
            append(" ".repeat(indent))
            append("Suppose\n")
            newAtomicHyps.forEach { hyp ->
                append(hyp.display(indent + 1))
                append("\n")
            }
            append(" ".repeat(indent))
            append("Then\n")
            newAtomicGoals.forEach { goal ->
                append(goal.display(indent + 1))
                append("\n")
            }
        }

}
