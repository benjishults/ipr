package bps.ipr.prover.tableau.preorder

import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.IdempotentSubstitution

object SimplePreorderNodeClosingAlgorithm {
    /**
     * @return an empty sequence if there is no substitution under [substitution] that closes the tree rooted at this node.
     * Otherwise, returns a sequence of substitutions under [substitution] that close the tree rooted at this node.
     * @param substitution `null` means that no node has been closed so far, so any substitution is valid.
     */
    fun BaseTableauNode.attemptCloseNode(
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
                        val childrensPositiveAtomsAbove: List<PositiveAtomicFormula> =
                            positiveAtomsAbove + newAtomicHyps
                        val childrensNegativeAtomsAbove: List<NegativeAtomicFormula> =
                            negativeAtomsAbove + newAtomicGoals
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
                                        nextChildNode.attemptCloseNode(
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

    fun BaseTableauNode.sequenceOfUnifiersHere(
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

}
