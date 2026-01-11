package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.tableau.display.DisplayTableauListener
import bps.ipr.prover.tableau.display.DisplayTableauTechRegistry
import bps.ipr.prover.tableau.listener.AddNodeToTableauListener
import bps.ipr.prover.tableau.closing.SimplePreorderTableauClosingAlgorithm
import bps.ipr.prover.tableau.rule.CategorizedSignedFormulas.Companion.categorizeSignedFormulas
import bps.ipr.prover.tableau.rule.FolRuleSelector
import bps.ipr.prover.tableau.rule.RuleSelector
import bps.ipr.prover.tableau.rule.SignedFormula
import kotlin.reflect.KClass

/**
 * This class is not thread-safe.
 */
open class BaseTableau(
    initialQLimit: Int = 1,
    val closingAlgorithm: Tableau<BaseTableauNode>.(FormulaUnifier) -> FolProofSuccess?,
) : Tableau<BaseTableauNode> {

    private var _root: BaseTableauNode? = null

    /**
     * Can only be set once.
     */
    override var root: BaseTableauNode
        get() = _root!!
        set(value) {
            if (_root === null) {
                _root = value
                registerNode(value)
            } else
                throw IllegalStateException("Root already set")
        }

    override val applicableRules: RuleSelector = FolRuleSelector(initialQLimit)

    private var _size: Long = 0

    val size: Long
        get() = _size

    val displayListenersMap: MutableMap<KClass<*>, DisplayTableauListener> = mutableMapOf()

    private val addNodeToTableauListeners: MutableList<AddNodeToTableauListener> = mutableListOf()

    fun addDisplayTableauListener(listener: DisplayTableauListener) {
        require(displayListenersMap.contains(listener::class).not()) {
            "Listener of type ${listener::class.simpleName} already registered on node $this"
        }
        displayListenersMap[listener::class] = listener
    }

    fun addAddNodeToTableauListener(listener: AddNodeToTableauListener) {
        addNodeToTableauListeners.add(listener)
    }

    private fun notifyDisplayListeners(appendable: Appendable, displayKey: String) {
        DisplayTableauTechRegistry
            .getClassForKeyOrNull(displayKey)
            ?.let { klass: KClass<*> ->
                displayListenersMap[klass]
                    ?.let { listener: DisplayTableauListener ->
                        try {
                            listener.displayTableau(appendable)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }
    }

    protected fun notifyAddNodeToTableauListeners(node: BaseTableauNode) =
        addNodeToTableauListeners.forEach { listener: AddNodeToTableauListener ->
            try {
                listener.addNodeToTableau(node)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    override fun attemptClose(formulaUnifier: FormulaUnifier): FolProofSuccess? =
        closingAlgorithm(formulaUnifier)

    fun registerNode(node: BaseTableauNode) {
        node.tableau = this
        node.id = _size++
        notifyAddNodeToTableauListeners(node)
    }

    override fun display(appendable: Appendable, displayKey: String) =
        when (displayKey) {
            "plain" ->
                root.preOrderTraverse { node: BaseTableauNode ->
                    appendable.appendLine("---")
                    appendable.append(node.displayNode())
                }
            else -> notifyDisplayListeners(appendable, displayKey)
        }

    fun setRootForFormula(
        formula: FolFormula,
        formulaImplementation: FolFormulaImplementation,
    ) {
        BaseTableauNode()
            .also { root: BaseTableauNode ->
                this.root = root
                SignedFormula
                    .create(
                        formula = formula,
                        sign = false,
                        birthPlace = root,
                        formulaImplementation = formulaImplementation,
                        parentFormula = null,
                    )
                    .reduceAlpha(
                        birthPlace = root,
                        parent = null,
                    )
                    .also {
                        val (pos, neg, closing, betas, deltas, gammas) = it.categorizeSignedFormulas()
                        root.populate(
                            newAtomicHyps = pos,
                            newAtomicGoals = neg,
                            closing = closing,
                            betas = betas,
                            deltas = deltas,
                            gammas = gammas,
                        )
                    }
            }

    }

    override fun toString(): String = StringBuilder().also { display(it) }.toString()

}
