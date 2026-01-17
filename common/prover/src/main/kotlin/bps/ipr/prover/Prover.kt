package bps.ipr.prover

import bps.ipr.formulas.Formula
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.Tableau
import bps.ipr.prover.tableau.closing.CondensingBranchCloser
import bps.ipr.prover.tableau.closing.FolBranchCloser

interface Prover<in F : Formula, out R : ProofResult> {

    fun prove(formula: F): R

}

interface ProofResult

sealed interface FolProofResult<C : FolBranchCloser> : ProofResult

open class FolProofSuccess<C : FolBranchCloser>(
    val branchCloser: C,
) : FolProofResult<C>

open class FolTableauProofSuccess<C>(
    branchCloser: C,
    val tableau: Tableau<BaseTableauNode, C>,
) : FolProofSuccess<C>(branchCloser) where C : FolBranchCloser, C : CondensingBranchCloser

open class FolProofFailure<C : FolBranchCloser> : FolProofResult<C>

interface FolProofIncomplete<C> : FolProofResult<C> where C : FolBranchCloser, C : CondensingBranchCloser {
    val tableau: Tableau<BaseTableauNode, C>
}

open class FolTableauProofIncomplete<C>(
    override val tableau: Tableau<BaseTableauNode, C>,
) : FolProofIncomplete<C> where C : FolBranchCloser, C : CondensingBranchCloser
