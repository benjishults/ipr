package bps.ipr.prover

import bps.ipr.formulas.Formula
import bps.ipr.prover.tableau.BaseTableau
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.Tableau
import bps.ipr.substitution.Substitution

interface Prover<in F: Formula, out R: ProofResult> {

    fun prove(formula: F): R

}

interface ProofResult

sealed interface FolProofResult : ProofResult

open class FolProofSuccess(
    val substitution: Substitution,
) : FolProofResult

open class FolTableauProofSuccess(
    substitution: Substitution,
    val tableau: Tableau<BaseTableauNode>,
) : FolProofSuccess(substitution)

data object FolProofFailure : FolProofResult

interface FolProofIncomplete: FolProofResult {
    companion object: FolProofIncomplete
}

open class FolTableauProofIncomplete(
    val tableau: BaseTableau,
) : FolProofIncomplete
