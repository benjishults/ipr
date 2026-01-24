package bps.ipr.prover.tableau.closing

import bps.ipr.common.IdentitySet
import bps.ipr.common.ImpossibleError
import bps.ipr.common.MutableIdentitySet
import bps.ipr.common.Node
import bps.ipr.common.emptyIdentitySet
import bps.ipr.common.identitySetOf
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.ClosingFormula
import bps.ipr.prover.tableau.rule.NegativeAtomicFormula
import bps.ipr.prover.tableau.rule.PositiveAtomicFormula
import bps.ipr.prover.tableau.rule.SignedFormula
import bps.ipr.substitution.IdempotentSubstitution

/**
 * This is only needed if you are condensing and you want to display the resulting condensed proof.
 */
data class DetailedFolCondensingBranchCloser(
    val condensingBranchCloser: CondensingFolBranchCloserImpl,
    /**
     * This will contain either the root node or closed children of splits.
     */
    val closed: IdentitySet<BaseTableauNode>,
    /**
     * This will contain either the root node (for an empty closer) or children of splits that need to be closed.
     */
    val needToClose: IdentitySet<BaseTableauNode>,
) : CondensingBranchCloser by condensingBranchCloser, FolBranchCloser by condensingBranchCloser {

    /**
     * This is to be called when the first leaf is closed.
     * @param condensingBranchCloser the condensing branch closer of the single leaf
     * @param involved the SignedFormulas involved in the closing of the single leaf
     */
    constructor(
        condensingBranchCloser: CondensingFolBranchCloserImpl,
        involved: Iterable<SignedFormula<*>>,
    ) : this(
        condensingBranchCloser = condensingBranchCloser,
        closed =
            involved
                .maxBy { it.birthPlace.depth }
                .let { deepestNewb: SignedFormula<*> ->
                    if (condensingBranchCloser.splits.isEmpty()) {
                        identitySetOf(deepestNewb.birthPlace.root)
                    } else {
                        identitySetOf(
                            highestBelowLowestSplit(
                                deepNode = deepestNewb.birthPlace,
                                splits = condensingBranchCloser.splits,
                            ),
                        )
                    }
                },
        needToClose =
            involved
                .maxBy { it.birthPlace.depth }
                .let { deepestNewb: SignedFormula<*> ->
                    if (condensingBranchCloser.splits.isEmpty()) {
                        emptyIdentitySet()
                    } else {
                        var deepNode: BaseTableauNode = deepestNewb.birthPlace
                        if (deepNode.parent === null) {
                            emptyIdentitySet()
                        } else
                            generateSequence {
                                deepNode
                                    .parent
//                                ?.let { deepNode to deepNode.parent }
                            }
                                .fold(
                                    if (deepNode.parent!! in condensingBranchCloser.splits) {
                                        MutableIdentitySet<BaseTableauNode>()
                                            .apply {
                                                deepNode
                                                    .parent!!
                                                    .children
                                                    .forEach {
                                                        if (it !== deepNode)
                                                            add(it)
                                                    }
                                            }
                                    } else {
                                        MutableIdentitySet()
                                    },
                                ) { acc: MutableIdentitySet<BaseTableauNode>, node: BaseTableauNode ->
                                    if (node.parent !== null) {
                                        if (node.parent!! in condensingBranchCloser.splits) {
                                            node
                                                .parent!!
                                                .children
                                                .forEach {
                                                    if (it !== node)
                                                        acc.add(it)
                                                }
                                        }
                                    }
                                    deepNode = node
                                    acc
                                }
                    }
                },
    )

}

// TODO here and elsewhere, move these methods with nullable receivers
//      inside the classes and have the caller deal with nulls better?
fun DetailedFolCondensingBranchCloser?.extendWith(
    hyp: PositiveAtomicFormula,
    goal: NegativeAtomicFormula,
    sub: IdempotentSubstitution,
): DetailedFolCondensingBranchCloser =
    if (this === null) {
        DetailedFolCondensingBranchCloser(
            condensingBranchCloser =
                (null as CondensingFolBranchCloserImpl?)
                    .extendWith(hyp, goal, sub),
            involved =
                Node(hyp, Node(goal)),
        )
    } else {
        condensingBranchCloser
            .extendWith(hyp, goal, sub)
            .let { branchCloser: CondensingFolBranchCloserImpl ->
                Node(hyp, Node(goal))
                    .extendClosedAndNot(this, branchCloser)
            }
    }

fun DetailedFolCondensingBranchCloser?.extendWith(
    closingFormula: ClosingFormula<*>,
): DetailedFolCondensingBranchCloser =
    if (this === null) {
        DetailedFolCondensingBranchCloser(
            condensingBranchCloser =
                (null as CondensingFolBranchCloserImpl?)
                    .extendWith(closingFormula),
            involved =
                Node(closingFormula),
        )
    } else {
        this
            .condensingBranchCloser
            .extendWith(closingFormula)
            .let { branchCloser: CondensingFolBranchCloserImpl ->
                Node(closingFormula)
                    .extendClosedAndNot(this, branchCloser)
            }
    }

private fun allMyParentsChildrenAreNowClosed(
    newlyClosedBranchNode: BaseTableauNode,
    newClosed: MutableIdentitySet<BaseTableauNode>,
): Boolean =
    newlyClosedBranchNode
        .parent!!
        .children
        .all { it === newlyClosedBranchNode || it in newClosed }

private fun highestBelowLowestSplit(
    deepNode: BaseTableauNode,
    splits: IdentitySet<BaseTableauNode>,
): BaseTableauNode {
    var iteratingNode: BaseTableauNode = deepNode
    do {
        if (iteratingNode.parent === null || iteratingNode.parent in splits)
            return iteratingNode
        else
            iteratingNode = iteratingNode.parent!!
    } while (true)
}

private fun Iterable<SignedFormula<*>>.extendClosedAndNot(
    receiver: DetailedFolCondensingBranchCloser,
    branchCloser: CondensingFolBranchCloserImpl,
): DetailedFolCondensingBranchCloser =
    maxBy { it.birthPlace.depth }
        .let { deepestNewb: SignedFormula<*> ->
            if (branchCloser.splits.isEmpty()) {
                // I think this can't happen if things are well-formed.  If it could, I would do:
                // identitySetOf(deepestNewb.birthPlace.root) to emptyIdentitySet<BaseTableauNode>()
                throw ImpossibleError("If there were not splits, then the receiver would not be being extended.")
            } else {
                var newlyClosedBranchNode: BaseTableauNode =
                    highestBelowLowestSplit(deepestNewb.birthPlace, branchCloser.splits)
                val newClosed: MutableIdentitySet<BaseTableauNode> =
                    MutableIdentitySet<BaseTableauNode>()
                        .apply { receiver.closed.forEach { add(it) } }
                val newNeedToClose: MutableIdentitySet<BaseTableauNode> =
                    MutableIdentitySet<BaseTableauNode>()
                        .apply { receiver.needToClose.forEach { add(it) } }
                while (newlyClosedBranchNode.parent !== null) {
                    // NOTE this should always be true because I'm always calling highestBelowLowestSplit
//                                if (newlyClosedBranchNode.parent!! in branchCloser.splits) {
                    if (allMyParentsChildrenAreNowClosed(
                            newlyClosedBranchNode = newlyClosedBranchNode,
                            newClosed = newClosed,
                        )
                    ) {
                        newNeedToClose.remove(newlyClosedBranchNode)
                        newClosed.removeAll { it in newlyClosedBranchNode.parent!!.children }
                        newlyClosedBranchNode =
                            highestBelowLowestSplit(newlyClosedBranchNode.parent!!, branchCloser.splits)
                    } else {
                        newClosed.add(newlyClosedBranchNode)
                        newNeedToClose.remove(newlyClosedBranchNode)
                    }
                }
                newClosed.add(newlyClosedBranchNode)
                newClosed to newNeedToClose
                DetailedFolCondensingBranchCloser(
                    condensingBranchCloser = branchCloser,
                    closed = newClosed,
                    needToClose = newNeedToClose,
                )
            }
        }
