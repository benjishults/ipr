package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.tableau.rule.CategorizedSignedFormulas.Companion.categorizeSignedFormulas
import bps.ipr.prover.tableau.rule.FolRuleSelector
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.RuleAddedListener
import bps.ipr.prover.tableau.rule.RuleDequeueListener
import bps.ipr.prover.tableau.rule.RuleSelector
import bps.ipr.prover.tableau.rule.SignedFormula
import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.IdempotentSubstitution
import kotlin.sequences.emptySequence

interface Tableau<out N : TableauNode> {
    val root: N
    val applicableRules: RuleSelector
    fun attemptClose(unifier: FormulaUnifier): FolProofSuccess?
}

/**
 * This class is not thread-safe.
 */
open class BaseTableau(
    initialQLimit: Int = 1,
//    ruleAddedListeners: List<RuleAddedListener> = emptyList(),
//    ruleDequeueListeners: List<RuleDequeueListener> = emptyList(),
) : Tableau<BaseTableauNode> {

    private val addNodeToTableauListeners: MutableList<AddNodeToTableauListener> = mutableListOf()

    fun addAddNodeToTableauListener(listener: AddNodeToTableauListener) {
        addNodeToTableauListeners.add(listener)
    }

    private fun notifyAddNodeToTableauListeners(node: BaseTableauNode) =
        addNodeToTableauListeners.forEach { listener ->
            try {
                listener.addNodeToTableau(node)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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

    fun registerNode(node: BaseTableauNode) {
        node.tableau = this
        node.id = _size++
        notifyAddNodeToTableauListeners(node)
    }

    override fun attemptClose(unifier: FormulaUnifier): FolProofSuccess? {
        return root
            .attemptClose(
                substitution = null,
                positiveAtomsAbove = emptyList(),
                negativeAtomsAbove = emptyList(),
                formulaUnifier = unifier,
            )
            .firstOrNull()
            ?.let { FolProofSuccess(it) }
    }

    /**
     * @return an empty sequence if there is no substitution under [substitution] that closes the tree rooted at this node.
     * Otherwise, returns a sequence of substitutions under [substitution] that close the tree rooted at this node.
     * @param substitution `null` means that no node has been closed so far, so any substitution is valid.
     */
    private fun BaseTableauNode.attemptClose(
        substitution: IdempotentSubstitution?,
        positiveAtomsAbove: List<PositiveAtomicFormula>,
        negativeAtomsAbove: List<NegativeAtomicFormula>,
        formulaUnifier: FormulaUnifier,
    ): Sequence<IdempotentSubstitution> =
        if (closables.isNotEmpty()) {
            return sequenceOf(substitution ?: EmptySubstitution)
        } else {
            sequenceOfUnifiersHere(positiveAtomsAbove, negativeAtomsAbove, substitution, formulaUnifier) +
                    if (children.isNotEmpty()) {
                        val childrensPositiveAtomsAbove = positiveAtomsAbove + newAtomicHyps
                        val childrensNegativeAtomsAbove = negativeAtomsAbove + newAtomicGoals
                        children
                            .asSequence()
                            .drop(1)
                            .fold(
                                // take the sequence of unifiers from the first child
                                children
                                    .first()
                                    .let { firstChild: BaseTableauNode ->
                                        firstChild
                                            .attemptClose(
                                                substitution = substitution,
                                                positiveAtomsAbove = childrensPositiveAtomsAbove,
                                                negativeAtomsAbove = childrensNegativeAtomsAbove,
                                                formulaUnifier = formulaUnifier,
                                            )
                                    },
                            ) { sequenceOfUnifiersOfPreviousChildren: Sequence<IdempotentSubstitution>, nextChildNode: BaseTableauNode ->
                                // create a sequence of unifiers compatible with closers of the previous children
                                sequenceOfUnifiersOfPreviousChildren
                                    .flatMap { sub: IdempotentSubstitution ->
                                        nextChildNode.attemptClose(
                                            substitution = sub,
                                            positiveAtomsAbove = childrensPositiveAtomsAbove,
                                            negativeAtomsAbove = childrensNegativeAtomsAbove,
                                            formulaUnifier = formulaUnifier,
                                        )
                                    }
                            }
                    } else
                        emptySequence()

        }

    private fun BaseTableauNode.sequenceOfUnifiersHere(
        positiveAtomsAbove: List<PositiveAtomicFormula>,
        negativeAtomsAbove: List<NegativeAtomicFormula>,
        substitution: IdempotentSubstitution?,
        formulaUnifier: FormulaUnifier,
    ): Sequence<IdempotentSubstitution> =
        newAtomicHyps
            .asSequence()
            .flatMap { newHyp: PositiveAtomicFormula ->
                (negativeAtomsAbove.asSequence() + newAtomicGoals.asSequence())
                    .mapNotNull { goalAbove: NegativeAtomicFormula ->
                        formulaUnifier.unify(
                            formula1 = newHyp.formula,
                            formula2 = goalAbove.formula,
                            under = substitution ?: EmptySubstitution,
                        )
                    }
            } +
                newAtomicGoals
                    .asSequence()
                    .flatMap { newGoal: NegativeAtomicFormula ->
                        positiveAtomsAbove
                            .asSequence()
                            .mapNotNull { hypAbove: PositiveAtomicFormula ->
                                formulaUnifier.unify(
                                    formula1 = newGoal.formula,
                                    formula2 = hypAbove.formula,
                                    under = substitution ?: EmptySubstitution,
                                )
                            }
                    }

    fun display(): String =
        buildString {
            root.preOrderTraverse { node: BaseTableauNode ->
                appendLine("---")
                append(node.display(2 * node.depth()))
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
//            ruleAddedListeners: List<RuleAddedListener> = emptyList(),
//            ruleDequeueListeners: List<RuleDequeueListener> = emptyList(),
        ): BaseTableau {
            return BaseTableau(initialQLimit/*, ruleAddedListeners, ruleDequeueListeners*/)
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
                                )
                                .reduceAlpha(birthPlace = root)
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

}
