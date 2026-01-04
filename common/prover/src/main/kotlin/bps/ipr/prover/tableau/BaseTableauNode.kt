package bps.ipr.prover.tableau

import bps.ipr.common.Queue
import bps.ipr.common.queue
import bps.ipr.prover.tableau.rule.BetaFormula
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.DeltaFormula
import bps.ipr.prover.tableau.rule.GammaFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.NegativeSignedFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveSignedFormula
import bps.ipr.prover.tableau.rule.SignedFormula

fun interface PopulateNodeWithFormulasListener {
    fun populateNodeWithFormulas(
        /*node: BaseTableauNode,*/
        newAtomicHyps: List<PositiveAtomicFormula>?,
        newAtomicGoals: List<NegativeAtomicFormula>?,
        closing: List<ClosingFormula<*>>?,
        betas: List<BetaFormula<*>>?,
        deltas: List<DeltaFormula<*>>?,
        gammas: List<GammaFormula<*>>?,
    )
}

/**
 * This class is not thread-safe.
 */
open class BaseTableauNode(
    parent: BaseTableauNode? = null,
) : TableauNode {

    private val populateListeners: MutableList<PopulateNodeWithFormulasListener> = mutableListOf()
    private val displayHypsListeners: MutableList<DisplayHypsListener> = mutableListOf()
    private val displayGoalsListeners: MutableList<DisplayGoalsListener> = mutableListOf()

    fun addPopulateListener(listener: PopulateNodeWithFormulasListener) {
        populateListeners.add(listener)
    }

    fun addDisplayHypsListener(listener: DisplayHypsListener) {
        displayHypsListeners.add(listener)
    }

    fun addDisplayGoalsListener(listener: DisplayGoalsListener) {
        displayGoalsListeners.add(listener)
    }

    private fun notifyPopulateListeners(
        newAtomicHyps: List<PositiveAtomicFormula>? = null,
        newAtomicGoals: List<NegativeAtomicFormula>? = null,
        closing: List<ClosingFormula<*>>? = null,
        betas: List<BetaFormula<*>>? = null,
        deltas: List<DeltaFormula<*>>? = null,
        gammas: List<GammaFormula<*>>? = null,
    ) =
        populateListeners.forEach {
            try {
                it.populateNodeWithFormulas(
                    newAtomicHyps = newAtomicHyps,
                    newAtomicGoals = newAtomicGoals,
                    closing = closing,
                    betas = betas,
                    deltas = deltas,
                    gammas = gammas,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun notifyDisplayHypsListeners(builder: StringBuilder, indent: Int) =
        displayHypsListeners.forEach {
            try {
                it.displayHyps(builder, indent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun notifyDisplayGoalsListeners(builder: StringBuilder, indent: Int) =
        displayGoalsListeners.forEach {
            try {
                it.displayGoals(builder, indent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private var _tableau: BaseTableau? = null

    /**
     * Setting this registers the node in the tableau which includes incrementing the size of the tableau and
     * setting the [id] of the node.
     */
    override var tableau: BaseTableau
        get() = _tableau!!
        set(value) {
            if (_tableau === null) {
                _tableau = value
                // NOTE no need to register the node here because this is called by registerNode
            } else
                throw IllegalStateException("Tableau already set")
        }

    private var _id: Long? = null

    /**
     * Can only be set once.
     */
    var id: Long
        get() = _id!!
        set(value) {
            if (_id === null)
                _id = value
            else
                throw IllegalStateException("Id already set")
        }

    private var _parent: BaseTableauNode? = parent
    val parent: BaseTableauNode?
        get() = _parent

    private var _children: List<BaseTableauNode> = emptyList()
    override val children: List<BaseTableauNode>
        get() = _children

    private var _newAtomicHyps: List<PositiveAtomicFormula>? = null
    override val newAtomicHyps: List<PositiveAtomicFormula>
        get() = _newAtomicHyps ?: emptyList()

    private var _newAtomicGoals: List<NegativeAtomicFormula>? = null
    override val newAtomicGoals: List<NegativeAtomicFormula>
        get() = _newAtomicGoals ?: emptyList()

    private var _closables: List<ClosingFormula<*>>? = null
    override val closables: List<ClosingFormula<*>>
        get() = _closables ?: emptyList()

    open fun populate(
        newAtomicHyps: List<PositiveAtomicFormula>?,
        newAtomicGoals: List<NegativeAtomicFormula>?,
        closing: List<ClosingFormula<*>>?,
        betas: List<BetaFormula<*>>?,
        deltas: List<DeltaFormula<*>>?,
        gammas: List<GammaFormula<*>>?,
    ) {
        if (_newAtomicHyps === null && _newAtomicGoals === null) {
            _newAtomicHyps = newAtomicHyps
            _newAtomicGoals = newAtomicGoals
            _closables = closing
            betas?.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            deltas?.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            gammas?.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            closing?.forEach { form: SignedFormula<*> ->
                this.tableau.applicableRules.addRule(form)
            }
            notifyPopulateListeners(newAtomicHyps, newAtomicGoals, closing, betas, deltas, gammas)
        } else {
            throw IllegalStateException("New hyps and new goals already set")
        }
    }

    fun leaves(): List<BaseTableauNode> =
        // NOTE a depth-first implementation with flatmap creates a lot of garbage
        buildList {
            breadthFirstTraverse {
                if (it.children.isEmpty())
                    add(it)
            }
        }

    fun createChildNodes(): List<BaseTableauNode> =
        children
            .takeIf { it.isNotEmpty() }
            ?.flatMap { it.createChildNodes() }
            ?: listOf(BaseTableauNode(parent = this))
                .also { _children = it }

    fun depth(): Int =
        parent
            ?.let { 1 + it.depth() }
            ?: 0

    fun <T : Any> preOrderAccumulateByBranch(accumulator: T, operation: BaseTableauNode.(T) -> T?): Boolean =
        operation(accumulator)
            ?.let { accumulatedValue: T ->
                children
                    .all {
                        it.preOrderAccumulateByBranch(accumulatedValue, operation)
                    }
            }
            ?: false

    fun <T : Any> postOrderTraverse(operation: (BaseTableauNode) -> T?): T? {
        children.forEach {
            val result: T? = it.postOrderTraverse(operation)
            if (result === null)
                return null
        }
        return operation(this)
    }

    fun preOrderTraverse(operation: (BaseTableauNode) -> Unit) {
        operation(this)
        children.forEach { it.preOrderTraverse(operation) }
    }

    inline fun breadthFirstTraverse(operation: (BaseTableauNode) -> Unit) =
        queue<BaseTableauNode>()
            .apply { enqueue(this@BaseTableauNode) }
            .let { queue: Queue<BaseTableauNode> ->
                while (true) {
                    val next: BaseTableauNode? = queue.dequeueOrNull()
                    if (next !== null) {
                        operation(next)
                        next
                            .children
                            .forEach { queue.enqueue(it) }
                    } else
                        break
                }
            }

    fun setChildren(newChildrenList: List<BaseTableauNode>) {
        if (_children.isEmpty()) {
            _children = newChildrenList
            newChildrenList.forEach { newChildNode: BaseTableauNode ->
                newChildNode._parent = this@BaseTableauNode
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
                appendLine(hyp.display(indent + 1))
                notifyDisplayHypsListeners(this, indent)
            }
            append(" ".repeat(indent))
            appendLine("Then")
            newAtomicGoals.forEach { goal ->
                appendLine(goal.display(indent + 1))
                notifyDisplayGoalsListeners(this, indent)
            }
        }

    override fun toString(): String = display(0)

}

fun interface DisplayHypsListener {
    fun displayHyps(builder: StringBuilder, indent: Int)
}

fun interface DisplayGoalsListener {
    fun displayGoals(builder: StringBuilder, indent: Int)
}

class DisplayableTableauNodeHelper :
    PopulateNodeWithFormulasListener,
    DisplayHypsListener,
    DisplayGoalsListener {

    val nonAtomicGoals: MutableList<NegativeSignedFormula<*>> = mutableListOf()
    val nonAtomicHyps: MutableList<PositiveSignedFormula<*>> = mutableListOf()
//    val closing: MutableList<ClosingFormula<*>> = mutableListOf()
//    val betas: MutableList<BetaFormula<*>> = mutableListOf()
//    val deltas: MutableList<DeltaFormula<*>> = mutableListOf()
//    val gammas: MutableList<GammaFormula<*>> = mutableListOf()

    private fun <T: SignedFormula<*>> distributor(formula: T) {
        when (formula) {
            is NegativeSignedFormula<*> -> nonAtomicGoals.add(formula)
            is PositiveSignedFormula<*> -> nonAtomicHyps.add(formula)
        }
    }

    override fun populateNodeWithFormulas(
        newAtomicHyps: List<PositiveAtomicFormula>?,
        newAtomicGoals: List<NegativeAtomicFormula>?,
        closing: List<ClosingFormula<*>>?,
        betas: List<BetaFormula<*>>?,
        deltas: List<DeltaFormula<*>>?,
        gammas: List<GammaFormula<*>>?,
    ) {
        closing
            ?.forEach(::distributor)
        betas
            ?.forEach(::distributor)
        deltas
            ?.forEach(::distributor)
        gammas
            ?.forEach(::distributor)
    }

    override fun displayHyps(builder: StringBuilder, indent: Int) {
        nonAtomicHyps.forEach { builder.appendLine(it.display(indent)) }
    }

    override fun displayGoals(builder: StringBuilder, indent: Int) {
        nonAtomicGoals.forEach { builder.appendLine(it.display(indent)) }
    }
}
