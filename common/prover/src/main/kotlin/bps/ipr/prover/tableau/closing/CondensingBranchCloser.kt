package bps.ipr.prover.tableau.closing

import bps.ipr.prover.tableau.BaseTableauNode

interface CondensingBranchCloser : BranchCloser {
    val splits: Set<BaseTableauNode>?
}
