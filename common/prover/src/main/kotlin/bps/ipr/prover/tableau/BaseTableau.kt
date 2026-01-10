package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.tableau.listener.AddNodeToTableauListener
import bps.ipr.prover.tableau.preorder.SimplePreorderTableauClosingAlgorithm
import bps.ipr.prover.tableau.rule.CategorizedSignedFormulas.Companion.categorizeSignedFormulas
import bps.ipr.prover.tableau.rule.FolRuleSelector
import bps.ipr.prover.tableau.rule.RuleSelector
import bps.ipr.prover.tableau.rule.SignedFormula

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

    private val addNodeToTableauListeners: MutableList<AddNodeToTableauListener> = mutableListOf()

    fun addAddNodeToTableauListener(listener: AddNodeToTableauListener) {
        addNodeToTableauListeners.add(listener)
    }

    protected fun notifyAddNodeToTableauListeners(node: BaseTableauNode) =
        addNodeToTableauListeners.forEach { listener ->
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

    override fun display(): String =
        buildString {
            root.preOrderTraverse { node: BaseTableauNode ->
                appendLine("---")
                append(node.display(node.depth()))
            }
        }

    override fun toString(): String = display()

    companion object {
        // NOTE had to do this outside a constructor because I have to have the generic function
        operator fun <T : FolFormula> invoke(
            formula: T,
            formulaImplementation: FolFormulaImplementation,
            initialQLimit: Int = 1,
            addNodeToTableauListeners: List<AddNodeToTableauListener>? = null,
            closingAlgorithm: (Tableau<BaseTableauNode>.(FormulaUnifier) -> FolProofSuccess?)? = null,
        ): BaseTableau {
            return BaseTableau(
                initialQLimit,
                closingAlgorithm = closingAlgorithm ?: { formulaUnifier ->
                    with(SimplePreorderTableauClosingAlgorithm) { attemptCloseSimplePreorder(formulaUnifier) }
                },
            )
                .also { tableau: BaseTableau ->
                    addNodeToTableauListeners
                        ?.forEach { addNodeToTableauListener ->
                            tableau.addAddNodeToTableauListener(addNodeToTableauListener)
                        }
                    BaseTableauNode()
                        .also { root: BaseTableauNode ->
                            tableau.root = root
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
//                                root.newGoals = neg
                                }
//                        .forEach { signedFormula ->
//                            applicableRules.addRule(signedFormula)
//                        }
                        }
                }
        }
    }

    fun displayToDot(): String =
        buildString {
            appendLine("digraph G {")
            appendLine("node [shape=box]")
            root.breadthFirstTraverse { node: BaseTableauNode ->
                append(node.displayToDot())
                if (node.parent != null)
                    appendLine(""""${node.parent!!.id}" -> "${node.id}"""")
            }
            appendLine("}")
        }

}
