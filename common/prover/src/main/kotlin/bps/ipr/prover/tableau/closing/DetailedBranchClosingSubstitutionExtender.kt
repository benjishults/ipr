package bps.ipr.prover.tableau.closing

import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.IdempotentSubstitution

object DetailedBranchClosingSubstitutionExtender : BranchCloserExtender<DetailedFolCondensingBranchCloser> {
    override fun DetailedFolCondensingBranchCloser?.extendBy(
        hyp: PositiveAtomicFormula,
        goal: NegativeAtomicFormula,
        sub: IdempotentSubstitution,
    ): DetailedFolCondensingBranchCloser =
        this.extendWith(hyp, goal, sub)

    override fun DetailedFolCondensingBranchCloser?.extendBy(
        closingFormula: ClosingFormula<*>,
    ): DetailedFolCondensingBranchCloser =
        this.extendWith(closingFormula)
}
