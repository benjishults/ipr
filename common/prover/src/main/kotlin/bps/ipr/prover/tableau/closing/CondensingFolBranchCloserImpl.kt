package bps.ipr.prover.tableau.closing

import bps.ipr.common.IdentitySet
import bps.ipr.common.combineNullable
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.IdempotentSubstitution

/**
 * This is only needed if you want condensing (which you do)
 */
data class CondensingFolBranchCloserImpl(
    /**
     * This is all that's needed for closing a tableau.
     */
    override val substitution: IdempotentSubstitution,
    /**
     * A [split] is non-empty [Set] of [BaseTableauNode]s with multiple children or `null`.
     *
     * These are needed for the condensing algorithm.
     */
    override val splits: IdentitySet<BaseTableauNode>,
) : CondensingBranchCloser, FolBranchCloser

fun CondensingFolBranchCloserImpl?.extendWith(
    hyp: PositiveAtomicFormula,
    goal: NegativeAtomicFormula,
    sub: IdempotentSubstitution,
) =
    CondensingFolBranchCloserImpl(
        substitution = sub, // should this combine the subs?
        splits =
            hyp
                .splits
                ?.toIdentitySet()
                .combineNullable(goal.splits?.toIdentitySet())
                .combineNullable(this?.splits),
    )

fun CondensingFolBranchCloserImpl?.extendWith(
    closingFormula: ClosingFormula<*>,
) =
    CondensingFolBranchCloserImpl(
        substitution = this?.substitution ?: EmptySubstitution,
        splits =
            closingFormula
                .splits
                ?.toIdentitySet()
                .combineNullable(this?.splits),
    )

