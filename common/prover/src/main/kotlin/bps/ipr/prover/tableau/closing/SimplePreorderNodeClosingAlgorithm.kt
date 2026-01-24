package bps.ipr.prover.tableau.closing

import bps.ipr.common.IdentitySet
import bps.ipr.common.LinkedList
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.EmptySubstitution

object SimplePreorderNodeClosingAlgorithm {
    /**
     * @return an empty sequence if there is no substitution under [branchClosingSubstitution] that closes the tree rooted at this node.
     * Otherwise, returns a sequence of substitutions under [branchClosingSubstitution] that close the tree rooted at this node.
     * @param branchClosingSubstitution `null` means that no node has been closed so far, so any substitution is valid.
     */
    fun <C> BaseTableauNode.attemptCloseNode(
        branchClosingSubstitution: C?,
        formulaUnifier: FormulaUnifier,
        branchCloserExtender: BranchCloserExtender<C>,
    ): Sequence<C> where C : CondensingBranchCloser, C : FolBranchCloser =
        if (closables.isNotEmpty()) {
            closables
                .asSequence()
                .map { closingFormula: ClosingFormula<*> ->
                    with(branchCloserExtender) {
                        branchClosingSubstitution.extendBy(closingFormula)
                    }
                }
        } else {
            sequenceOfBranchClosersHere(branchClosingSubstitution, formulaUnifier, branchCloserExtender) +
                    sequenceOfBranchClosersOfChildren(branchClosingSubstitution, formulaUnifier, branchCloserExtender)
        }

    private fun <C> BaseTableauNode.sequenceOfBranchClosersOfChildren(
        branchClosingSubstitution: C?,
        formulaUnifier: FormulaUnifier,
        branchCloserExtender: BranchCloserExtender<C>,
    ): Sequence<C> where C : CondensingBranchCloser, C : FolBranchCloser =
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
                                    branchCloserExtender = branchCloserExtender,
                                )
                        },
                ) { sequenceOfUnifiersOfPreviousChildren: Sequence<C>, nextChildNode: BaseTableauNode ->
                    // create a sequence of unifiers compatible with closers of the previous children
                    sequenceOfUnifiersOfPreviousChildren
                        .flatMap { branchCloser: C ->
                            if (branchCloser
                                    .splits
                                    .let { splits: IdentitySet<BaseTableauNode> ->
                                        nextChildNode.parent in splits
                                    }
                            ) {
                                // if this node needs to be closed
                                nextChildNode
                                    .attemptCloseNode(
                                        branchClosingSubstitution = branchCloser,
                                        formulaUnifier = formulaUnifier,
                                        branchCloserExtender = branchCloserExtender,
                                    )
                            } else {
                                //   we wouldn't be here if the first child didn't have a closer
                                //   but we know that closer doesn't need this child to be added
                                sequenceOf(branchCloser)
                            }
                        }
                }
        } else {
            emptySequence()
        }

    fun <C> BaseTableauNode.sequenceOfBranchClosersHere(
        branchCloser: C?,
        formulaUnifier: FormulaUnifier,
        branchCloserExtender: BranchCloserExtender<C>,
    ): Sequence<C> where C : CondensingBranchCloser, C : FolBranchCloser =
        newAtomicHyps
            .asSequence()
            .flatMap { newHyp: PositiveAtomicFormula ->
                (LinkedList(parent?.negativeAtomsFromHereUp).asSequence() + newAtomicGoals.asSequence())
                    .mapNotNull { goalAbove: NegativeAtomicFormula ->
                        formulaUnifier.unify(
                            formula1 = newHyp.formula,
                            formula2 = goalAbove.formula,
                            under = branchCloser?.substitution ?: EmptySubstitution,
                        )?.let { sub ->
                            with(branchCloserExtender) {
                                branchCloser.extendBy(
                                    hyp = newHyp,
                                    goal = goalAbove,
                                    sub = sub,
                                )
                            }
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
                                    under = branchCloser?.substitution ?: EmptySubstitution,
                                )?.let { sub ->
                                    with(branchCloserExtender) {
                                        branchCloser.extendBy(
                                            hyp = hypAbove,
                                            goal = newGoal,
                                            sub = sub,
                                        )
                                    }
                                }
                            }

                    }

}
