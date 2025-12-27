package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.Formula
import bps.ipr.prover.tableau.ClosingRule
import bps.ipr.prover.tableau.NegativeAndRule
import bps.ipr.prover.tableau.NegativeForAllRule
import bps.ipr.prover.tableau.NegativeForSomeRule
import bps.ipr.prover.tableau.NegativeIffRule
import bps.ipr.prover.tableau.NegativeImpliesRule
import bps.ipr.prover.tableau.NegativeNotRule
import bps.ipr.prover.tableau.NegativeOrRule
import bps.ipr.prover.tableau.PositiveAndRule
import bps.ipr.prover.tableau.PositiveForAllRule
import bps.ipr.prover.tableau.PositiveForSomeRule
import bps.ipr.prover.tableau.PositiveIffRule
import bps.ipr.prover.tableau.PositiveImpliesRule
import bps.ipr.prover.tableau.PositiveNotRule
import bps.ipr.prover.tableau.PositiveOrRule
import bps.ipr.prover.tableau.Rule
import bps.ipr.prover.tableau.RuleSet
import bps.ipr.prover.tableau.SignedFormula
import bps.ipr.prover.tableau.Tableau
import bps.ipr.prover.tableau.TableauNode
import bps.ipr.terms.EmptySubstitution
import bps.ipr.terms.Substitution

interface Prover<T : FolFormula<T>, out R : ProofResult> {

    fun prove(formula: T): R

}

interface ProofResult {
}

sealed class FolProofResult : ProofResult

data class FolProofSuccess(
    val substitution: Substitution,
) : FolProofResult()

data object FolProofFailure : FolProofResult()

data object FolProofIncomplete : FolProofResult()

class TableauProver<T: FolFormula<T>> : Prover<T, FolProofResult> {

    override fun  prove(formula: T): FolProofResult =
        Tableau(formula)
            .let { tableau: Tableau ->
                tableau.applicableRules.getNextRule()
                    ?.let { rule: Rule<*> ->
                        when (rule) {
                            ClosingRule -> FolProofSuccess(EmptySubstitution)
                            else -> TODO()
                        }
                    }
                    ?: FolProofFailure

            }
}

