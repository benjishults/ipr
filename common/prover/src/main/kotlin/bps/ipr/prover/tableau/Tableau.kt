package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.prover.tableau.rule.CategorizedSignedFormulas.Companion.categorizeSignedFormulas
import bps.ipr.prover.tableau.rule.FolRuleSelector
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.RuleSelector
import bps.ipr.prover.tableau.rule.SignedFormula
import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.IdempotentSubstitution
import kotlin.sequences.emptySequence

/**
 * This class is not thread-safe.
 */
open class Tableau(
    initialQLimit: Int = 1,
) {

    private var _root: TableauNode? = null

    /**
     * Can only be set once.
     */
    var root: TableauNode
        get() = _root!!
        set(value) {
            if (_root === null) {
                _root = value
                registerNode(value)
            } else
                throw IllegalStateException("Root already set")
        }

    val applicableRules: RuleSelector = FolRuleSelector(initialQLimit)

    private var _size: Long = 0
    val size: Long
        get() = _size

    fun registerNode(node: TableauNode) {
        node.tableau = this
        node.id = _size++
    }

    fun attemptClose(unifier: FormulaUnifier): FolProofSuccess? {
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
    private fun TableauNode.attemptClose(
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
                                    .let { firstChild: TableauNode ->
                                        firstChild
                                            .attemptClose(
                                                substitution = substitution,
                                                positiveAtomsAbove = childrensPositiveAtomsAbove,
                                                negativeAtomsAbove = childrensNegativeAtomsAbove,
                                                formulaUnifier = formulaUnifier,
                                            )
                                    },
                            ) { sequenceOfUnifiersOfPreviousChildren: Sequence<IdempotentSubstitution>, nextChildNode: TableauNode ->
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

    private fun TableauNode.sequenceOfUnifiersHere(
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
            root.preOrderTraverse { node: TableauNode ->
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
        ): Tableau {
            return Tableau(initialQLimit)
                .also { tableau: Tableau ->
                    TableauNode()
                        .also { root: TableauNode ->
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
