package bps.ipr.prover

import bps.ipr.formulas.Formula
import bps.ipr.formulas.FormulaImplementation
import bps.ipr.substitution.Substitution

interface Prover<in F: Formula, in I: FormulaImplementation, out R: ProofResult> {

    fun prove(formula: F, formulaImplementation: I): R

}

interface ProofResult

sealed interface FolProofResult : ProofResult

data class FolProofSuccess(
    val substitution: Substitution,
) : FolProofResult

data object FolProofFailure : FolProofResult

data object FolProofIncomplete : FolProofResult
