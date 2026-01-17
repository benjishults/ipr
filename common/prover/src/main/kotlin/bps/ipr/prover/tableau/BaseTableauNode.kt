package bps.ipr.prover.tableau

import bps.ipr.common.Node
import bps.ipr.common.Queue
import bps.ipr.common.queue
import bps.ipr.prover.tableau.display.DisplayNodeListener
import bps.ipr.prover.tableau.display.DisplayNodeTechRegistry
import bps.ipr.prover.tableau.listener.PopulateNodeWithFormulasListener
import bps.ipr.prover.tableau.rule.BetaFormula
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.DeltaFormula
import bps.ipr.prover.tableau.rule.GammaFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.SignedFormula
import kotlin.reflect.KClass

/**
 * This class is not thread-safe.
 */
open class BaseTableauNode(
    parent: BaseTableauNode? = null,
) : TableauNode<BaseTableauNode> {

    var positiveAtomsFromHereUp: Node<PositiveAtomicFormula>? = null
        private set
    var negativeAtomsFromHereUp: Node<NegativeAtomicFormula>? = null
        private set

    var populateFormulasListeners: MutableList<PopulateNodeWithFormulasListener>? = null
        private set
    val displayNodeListenersMap: MutableMap<KClass<*>, DisplayNodeListener> = mutableMapOf()

    private var _tableau: BaseTableau<*>? = null

    /**
     * Setting this registers the node in the tableau which includes incrementing the size of the tableau and
     * setting the [id] of the node.
     */
    override var tableau: BaseTableau<*>
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
            populateFormulasListeners?.run {
                notifyPopulateListeners(
                    newAtomicHyps = newAtomicHyps,
                    newAtomicGoals = newAtomicGoals,
                    closing = closing,
                    betas = betas,
                    deltas = deltas,
                    gammas = gammas,
                )
            }
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

    val depth: Int =
        parent
            ?.let { 1 + it.depth }
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

    /**
     * Only generated when this node is about to have children
     */
    private fun generatePositiveAtomsFromHereUp(): Node<PositiveAtomicFormula>? =
        _newAtomicHyps
            ?.fold(null as Node<PositiveAtomicFormula>? to null as Node<PositiveAtomicFormula>?) {
                    headToTail: Pair<Node<PositiveAtomicFormula>?, Node<PositiveAtomicFormula>?>,
                    formula: PositiveAtomicFormula,
                ->
                Node(formula, headToTail.first)
                    .let { newbie ->
                        newbie to (headToTail.second ?: newbie)
                    }
            }
            ?.let { (head: Node<PositiveAtomicFormula>?, tail: Node<PositiveAtomicFormula>?) ->
                head
                    ?.also {
                        tail!!.next = parent?.positiveAtomsFromHereUp
                    }
                    ?: parent?.positiveAtomsFromHereUp
            }
            ?: parent?.positiveAtomsFromHereUp

    private fun generateNegativeAtomsFromHereUp(): Node<NegativeAtomicFormula>? =
        _newAtomicGoals
            ?.fold(null as Node<NegativeAtomicFormula>? to null as Node<NegativeAtomicFormula>?) {
                    headToTail: Pair<Node<NegativeAtomicFormula>?, Node<NegativeAtomicFormula>?>,
                    formula: NegativeAtomicFormula,
                ->
                Node(formula, headToTail.first)
                    .let { newbie ->
                        newbie to (headToTail.second ?: newbie)
                    }
            }
            ?.let { (head: Node<NegativeAtomicFormula>?, tail: Node<NegativeAtomicFormula>?) ->
                head
                    ?.also {
                        tail!!.next = parent?.negativeAtomsFromHereUp
                    }
                    ?: parent?.negativeAtomsFromHereUp
            }
            ?: parent?.negativeAtomsFromHereUp

    fun setChildren(newChildrenList: List<BaseTableauNode>) {
        if (_children.isEmpty()) {
            positiveAtomsFromHereUp = generatePositiveAtomsFromHereUp()
            negativeAtomsFromHereUp = generateNegativeAtomsFromHereUp()
            _children = newChildrenList
            newChildrenList.forEach { newChildNode: BaseTableauNode ->
                newChildNode._parent = this@BaseTableauNode
            }
        } else {
            throw IllegalStateException("Children already set")
        }
    }

    fun addPopulateListener(listener: PopulateNodeWithFormulasListener) {
        populateFormulasListeners?.add(listener) ?: run { populateFormulasListeners = mutableListOf(listener) }
    }

    fun addDisplayNodeListener(listener: DisplayNodeListener) {
        require(displayNodeListenersMap.contains(listener::class).not()) {
            "Listener of type ${listener::class.simpleName} already registered on node $this"
        }
        displayNodeListenersMap[listener::class] = listener
    }

    private fun notifyPopulateListeners(
        newAtomicHyps: List<PositiveAtomicFormula>? = null,
        newAtomicGoals: List<NegativeAtomicFormula>? = null,
        closing: List<ClosingFormula<*>>? = null,
        betas: List<BetaFormula<*>>? = null,
        deltas: List<DeltaFormula<*>>? = null,
        gammas: List<GammaFormula<*>>? = null,
    ) =
        populateFormulasListeners?.forEach {
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

    private fun notifyDisplayNodeListeners(appendable: Appendable, displayTechKey: String) {
        DisplayNodeTechRegistry
            .getClassForKeyOrNull(displayTechKey)
            ?.let { klass: KClass<*> ->
                displayNodeListenersMap[klass]
                    ?.let { listener: DisplayNodeListener ->
                        try {
                            listener.displayNode(appendable)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }
    }

    fun displayNode(displayTechKey: String = "plain"): String =
        when (displayTechKey) {
            "plain" -> display()
            else -> {
                buildString { notifyDisplayNodeListeners(this, displayTechKey) }
            }
        }

    private fun display(): String =
        buildString {
            append(" ".repeat(depth))
            if (newAtomicHyps.isNotEmpty()) {
                appendLine("(${id}) Suppose")
                newAtomicHyps.forEach { hyp: PositiveAtomicFormula ->
                    appendLine(hyp.display(depth + 1))
                }
                if (newAtomicGoals.isNotEmpty()) {
                    append(" ".repeat(depth))
                    appendLine("Show")
                }
            } else {
                append(" ".repeat(depth))
                appendLine("($id) Show")
            }
            newAtomicGoals.forEach { goal: NegativeAtomicFormula ->
                appendLine(goal.display(depth + 1))
            }
        }

    // NOTE do not override equals and hashCode.  We want to use identity.

    override fun toString(): String = displayNode()

}
