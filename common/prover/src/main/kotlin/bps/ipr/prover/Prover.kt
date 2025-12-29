package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.tableau.SignedFormula
import bps.ipr.prover.tableau.Tableau
import bps.ipr.terms.Substitution
import bps.ipr.terms.TermUnifier

interface Prover/*<T : FolFormula, out R : ProofResult>*/ {

    fun prove(formula: FolFormula): FolProofResult
//    fun prove(formula: T): R

}

interface ProofResult

sealed interface FolProofResult : ProofResult

data class FolProofSuccess(
    val substitution: Substitution,
) : FolProofResult

data object FolProofFailure : FolProofResult

data object FolProofIncomplete : FolProofResult

class TableauProver(
    val unifier: FormulaUnifier
) : Prover/*<FolFormula, FolProofResult>*/ {

    override fun prove(formula: FolFormula): FolProofResult =
        Tableau(formula)
            .let { tableau: Tableau ->
                var result: FolProofResult? = tableau.attemptClose(unifier)
                while (result === null) {
                    tableau
                        .applicableRules
                        .dequeueNextRuleOrNull()
                        ?.also { rule: SignedFormula<*> ->
                            rule.apply()
                            result = tableau.attemptClose(unifier)
                        }
                        ?: run { result = FolProofIncomplete }
                }
                result!!
            }
}

