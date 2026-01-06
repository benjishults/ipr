package bps.ipr.prover.tableau.preorder

import bps.ipr.common.LinkedList
import bps.ipr.common.Node
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.IdempotentSubstitution

object SimplePreorderNodeClosingAlgorithm {
    /**
     * @return an empty sequence if there is no substitution under [branchClosingSubstitution] that closes the tree rooted at this node.
     * Otherwise, returns a sequence of substitutions under [branchClosingSubstitution] that close the tree rooted at this node.
     * @param branchClosingSubstitution `null` means that no node has been closed so far, so any substitution is valid.
     */
    fun BaseTableauNode.attemptCloseNode(
        branchClosingSubstitution: BranchClosingSubstitution?,
        formulaUnifier: FormulaUnifier,
    ): Sequence<BranchClosingSubstitution> =
        if (closables.isNotEmpty()) {
            closables
                .asSequence()
                .map { closingFormula ->
                    BranchClosingSubstitution(
                        substitution = branchClosingSubstitution?.substitution ?: EmptySubstitution,
                        splits =
                            closingFormula
                                .splits
                                .combineNullable(
                                    branchClosingSubstitution?.splits,
                                ),
                    )
                }
        } else {
            sequenceOfUnifiersHere(branchClosingSubstitution, formulaUnifier) +
                    if (children.isNotEmpty()) {
                        children
                            .asSequence()
                            .drop(1)
                            .fold(
                                // take the sequence of unifiers from the first child
                                children
                                    .first()
                                    .let { firstChild: BaseTableauNode ->
                                        firstChild
                                            .attemptCloseNode(
                                                branchClosingSubstitution = branchClosingSubstitution,
                                                formulaUnifier = formulaUnifier,
                                            )
                                    },
                            ) { sequenceOfUnifiersOfPreviousChildren: Sequence<BranchClosingSubstitution>, nextChildNode: BaseTableauNode ->
                                // create a sequence of unifiers compatible with closers of the previous children
                                sequenceOfUnifiersOfPreviousChildren
                                    .flatMap { sub: BranchClosingSubstitution ->
                                        nextChildNode
                                            .attemptCloseNode(
                                                branchClosingSubstitution = sub,
                                                formulaUnifier = formulaUnifier,
                                            )
                                    }
                            }
                    } else
                        emptySequence()

        }

    fun BaseTableauNode.sequenceOfUnifiersHere(
        branchClosingSubstitution: BranchClosingSubstitution?,
        formulaUnifier: FormulaUnifier,
    ): Sequence<BranchClosingSubstitution> =
        newAtomicHyps
            .asSequence()
            .flatMap { newHyp: PositiveAtomicFormula ->
                (LinkedList(parent?.negativeAtomsFromHereUp).asSequence() + newAtomicGoals.asSequence())
                    .mapNotNull { goalAbove: NegativeAtomicFormula ->
                        formulaUnifier.unify(
                            formula1 = newHyp.formula,
                            formula2 = goalAbove.formula,
                            under = branchClosingSubstitution?.substitution ?: EmptySubstitution,
                        )?.let { sub ->
                            BranchClosingSubstitution(
                                substitution = sub,
                                splits = newHyp.splits.combineNullable(goalAbove.splits),
                            )
                        }
                    }
            } +
                newAtomicGoals
                    .asSequence()
                    .flatMap { newGoal: NegativeAtomicFormula ->
                        LinkedList(parent?.positiveAtomsFromHereUp)
                            .asSequence()
                            .mapNotNull { hypAbove: PositiveAtomicFormula ->
                                formulaUnifier.unify(
                                    formula1 = newGoal.formula,
                                    formula2 = hypAbove.formula,
                                    under = branchClosingSubstitution?.substitution ?: EmptySubstitution,
                                )?.let { sub ->
                                    BranchClosingSubstitution(
                                        substitution = sub,
                                        splits = hypAbove.splits.combineNullable(newGoal.splits),
                                    )
                                }
                            }

                    }


}

fun <T> List<T>?.combineNullable(
    rest: List<T>?,
): List<T>? =
    this?.let { rest?.plus(it) ?: it }

data class BranchClosingSubstitution(
    val substitution: IdempotentSubstitution,
    // val node: BaseTableauNode,
    /**
     * A [split] is non-empty [List] of [BaseTableauNode]s with multiple children or `null`.
     */
    val splits: Node<BaseTableauNode>?,
)
