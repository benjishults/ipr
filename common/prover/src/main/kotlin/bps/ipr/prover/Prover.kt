package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.Formula
import bps.ipr.prover.rule.ClosingRule
import bps.ipr.terms.EmptySubstitution
import bps.ipr.terms.Substitution

interface Prover<in T : Formula, out R : ProofResult> {

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

class TableauProver : Prover<FolFormula<*>, FolProofResult> {
    override fun prove(formula: FolFormula<*>): FolProofResult =
        Tableau(
            TableauNode(
                newGoals = listOf(SignedFormula(formula, false)),
            ),
        )
            .let { tableau: Tableau ->
                if (tableau.root.newGoals.any { it.rule == ClosingRule })
                    FolProofSuccess(EmptySubstitution)
                else
                    FolProofIncomplete
            }

}
