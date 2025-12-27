package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.prover.tableau.ClosingFormula
import bps.ipr.prover.tableau.SignedFormula
import bps.ipr.prover.tableau.Tableau
import bps.ipr.terms.EmptySubstitution
import bps.ipr.terms.Substitution

interface Prover<T : FolFormula, out R : ProofResult> {

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

class TableauProver : Prover<FolFormula, FolProofResult> {

    override fun prove(formula: FolFormula): FolProofResult =
        Tableau(formula)
            .let { tableau: Tableau ->
                tableau.applicableRules.getNextRule()
                    ?.let { rule: SignedFormula<*> ->
                        when (rule) {
                            is ClosingFormula -> FolProofSuccess(EmptySubstitution)
                            else -> TODO()
                        }
                    }
                    ?: FolProofFailure

            }
}

