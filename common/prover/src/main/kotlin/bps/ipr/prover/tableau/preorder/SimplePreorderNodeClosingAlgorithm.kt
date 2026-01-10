package bps.ipr.prover.tableau.preorder

import bps.ipr.common.LinkedList
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.ClosingFormula
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
                .map { closingFormula: ClosingFormula<*> ->
                    branchClosingSubstitution.extendBy(closingFormula)
                }
        } else {
            sequenceOfBranchClosersHere(branchClosingSubstitution, formulaUnifier) +
                    sequenceOfBranchClosersOfChildren(branchClosingSubstitution, formulaUnifier)
        }

    private fun BaseTableauNode.sequenceOfBranchClosersOfChildren(
        branchClosingSubstitution: BranchClosingSubstitution?,
        formulaUnifier: FormulaUnifier,
    ): Sequence<BranchClosingSubstitution> = if (children.isNotEmpty()) {
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
                        if (sub.splits?.let { nextChildNode.parent in it } == true) {
                            // if this node needs to be closed
                            nextChildNode
                                .attemptCloseNode(
                                    branchClosingSubstitution = sub,
                                    formulaUnifier = formulaUnifier,
                                )
                        } else {
                            //   we wouldn't be here if the first child didn't have a closer
                            //   but we know that closer doesn't need this child to be added
                            sequenceOf(sub)
                        }
                    }
            }
    } else
        emptySequence()

    fun BaseTableauNode.sequenceOfBranchClosersHere(
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
                            branchClosingSubstitution.extendBy(
                                hyp = newHyp,
                                goal = goalAbove,
                                sub = sub,
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
                                    branchClosingSubstitution.extendBy(
                                        hyp = hypAbove,
                                        goal = newGoal,
                                        sub = sub,
                                    )
                                }
                            }

                    }

}

fun <T> Set<T>?.combineNullable(
    rest: Set<T>?,
): Set<T>? =
    this
        .let { receiver ->
            rest
                ?.let { rest ->
                    if (receiver === null)
                        rest
                    else
                        rest.plus(receiver)
                }
                ?: receiver
        }

data class BranchClosingSubstitution(
    val substitution: IdempotentSubstitution,
    // val node: BaseTableauNode,
    /**
     * A [split] is non-empty [Set] of [BaseTableauNode]s with multiple children or `null`.
     */
    val splits: Set<BaseTableauNode>?,
)

fun BranchClosingSubstitution?.extendBy(
    hyp: PositiveAtomicFormula,
    goal: NegativeAtomicFormula,
    sub: IdempotentSubstitution,
) =
    BranchClosingSubstitution(
        substitution = sub,
        splits =
            hyp
                .splits
                ?.toSet()
                .combineNullable(goal.splits?.toSet())
                .combineNullable(this?.splits),
    )

fun BranchClosingSubstitution?.extendBy(
    closingFormula: ClosingFormula<*>,
) =
    BranchClosingSubstitution(
        substitution = this?.substitution ?: EmptySubstitution,
        splits =
            closingFormula
                .splits
                ?.toSet()
                .combineNullable(this?.splits),
    )
