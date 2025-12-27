package bps.ipr.prover

import bps.ipr.formulas.And
import bps.ipr.formulas.Equivalence
import bps.ipr.formulas.Falsity
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.Truth
import bps.ipr.prover.rule.AlphaRule
import bps.ipr.prover.rule.BetaRule
import bps.ipr.prover.rule.ClosingRule
import bps.ipr.prover.rule.DeltaRule
import bps.ipr.prover.rule.GammaRule
import bps.ipr.prover.rule.NegativeAndRule
import bps.ipr.prover.rule.NegativeForAllRule
import bps.ipr.prover.rule.NegativeForSomeRule
import bps.ipr.prover.rule.NegativeIffRule
import bps.ipr.prover.rule.NegativeImpliesRule
import bps.ipr.prover.rule.NegativeNotRule
import bps.ipr.prover.rule.NegativeOrRule
import bps.ipr.prover.rule.PositiveAndRule
import bps.ipr.prover.rule.PositiveForAllRule
import bps.ipr.prover.rule.PositiveForSomeRule
import bps.ipr.prover.rule.PositiveIffRule
import bps.ipr.prover.rule.PositiveImpliesRule
import bps.ipr.prover.rule.PositiveNotRule
import bps.ipr.prover.rule.PositiveOrRule
import bps.ipr.prover.rule.Rule

data class SignedFormula(
    val formula: FolFormula<*>,
    val sign: Boolean,
) {
    val rule: Rule? =
        if (sign) {
            when (formula) {
                is And -> PositiveAndRule
                is Or -> PositiveOrRule
                is Implies -> PositiveImpliesRule
                is Equivalence -> PositiveIffRule
                is ForAll -> PositiveForAllRule
                is ForSome -> PositiveForSomeRule
                is Not -> PositiveNotRule
                Falsity -> ClosingRule
                is Predicate -> null
                Truth -> null
            }
        } else {
            when (formula) {
                is And -> NegativeAndRule
                is Or -> NegativeOrRule
                is Implies -> NegativeImpliesRule
                is Equivalence -> NegativeIffRule
                is ForAll -> NegativeForAllRule
                is ForSome -> NegativeForSomeRule
                is Not -> NegativeNotRule
                Truth -> ClosingRule
                is Predicate -> null
                Falsity -> null
            }
        }
}
