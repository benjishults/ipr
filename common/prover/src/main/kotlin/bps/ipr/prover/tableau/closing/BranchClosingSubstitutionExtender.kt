package bps.ipr.prover.tableau.closing

import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.IdempotentSubstitution

object BranchClosingSubstitutionExtender : BranchCloserExtender<CondensingFolBranchCloserImpl> {
    override fun CondensingFolBranchCloserImpl?.extendBy(
        hyp: PositiveAtomicFormula,
        goal: NegativeAtomicFormula,
        sub: IdempotentSubstitution,
    ): CondensingFolBranchCloserImpl =
        this.extendWith(hyp, goal, sub)

    override fun CondensingFolBranchCloserImpl?.extendBy(
        closingFormula: ClosingFormula<*>,
    ): CondensingFolBranchCloserImpl =
        this.extendWith(closingFormula)
}
