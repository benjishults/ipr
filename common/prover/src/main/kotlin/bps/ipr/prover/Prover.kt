package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.tableau.SignedFormula
import bps.ipr.prover.tableau.Tableau
import bps.ipr.substitution.Substitution

interface Prover/*<T : FolFormula, out R : ProofResult>*/ {

    fun prove(formula: FolFormula, formulaImplementation: FolFormulaImplementation): FolProofResult
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
    val unifier: FormulaUnifier,
    val initialQLimit: Int = 1,
) : Prover/*<FolFormula, FolProofResult>*/ {

    override fun prove(formula: FolFormula, formulaImplementation: FolFormulaImplementation): FolProofResult =
        Tableau(formula, formulaImplementation, initialQLimit)
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

