package bps.ipr.prover.tableau.closing

import bps.ipr.common.combineNullable
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.SignedFormula
import bps.ipr.substitution.IdempotentSubstitution

/**
 * This is only needed if you are condensing and you want to display the resulting condensed proof.
 */
data class DetailedFolCondensingBranchCloser(
    val condensingBranchCloser: CondensingFolBranchCloserImpl,
    /**
     * Formulas involved in the closing.  These are needed for displaying the condensed tableau.
     */
    val involved: Set<SignedFormula<*>>,
) : CondensingBranchCloser by condensingBranchCloser, FolBranchCloser by condensingBranchCloser

fun DetailedFolCondensingBranchCloser?.extendWith(
    hyp: PositiveAtomicFormula,
    goal: NegativeAtomicFormula,
    sub: IdempotentSubstitution,
) =
    DetailedFolCondensingBranchCloser(
        condensingBranchCloser = this?.condensingBranchCloser.extendWith(hyp, goal, sub),
        involved = setOf(hyp, goal).combineNullable(this?.involved),
    )

fun DetailedFolCondensingBranchCloser?.extendWith(
    closingFormula: ClosingFormula<*>,
) =
    DetailedFolCondensingBranchCloser(
        condensingBranchCloser = this?.condensingBranchCloser.extendWith(closingFormula),
        involved = setOf(closingFormula).combineNullable(this?.involved),
    )
