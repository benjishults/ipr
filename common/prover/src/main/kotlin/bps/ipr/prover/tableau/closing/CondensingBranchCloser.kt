package bps.ipr.prover.tableau.closing

import bps.ipr.common.IdentitySet
import bps.ipr.prover.tableau.BaseTableauNode

interface CondensingBranchCloser : BranchCloser {
    val splits: IdentitySet<BaseTableauNode>
}
