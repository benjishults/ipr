package bps.ipr.prover.tableau.closing

import bps.ipr.substitution.IdempotentSubstitution

interface BranchCloser

interface FolBranchCloser : BranchCloser {
    val substitution: IdempotentSubstitution
}

data class FolBranchCloserImpl(
    override val substitution: IdempotentSubstitution,
) : FolBranchCloser
