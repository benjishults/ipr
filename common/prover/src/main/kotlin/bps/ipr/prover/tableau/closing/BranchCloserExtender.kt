package bps.ipr.prover.tableau.closing

import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.substitution.IdempotentSubstitution

interface BranchCloserExtender<C : BranchCloser> {
    fun C?.extendBy(
        hyp: PositiveAtomicFormula,
        goal: NegativeAtomicFormula,
        sub: IdempotentSubstitution,
    ): C

    fun C?.extendBy(
        closingFormula: ClosingFormula<*>,
    ): C
}
