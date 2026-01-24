package bps.ipr.prover.tableau.display.closer

//import bps.ipr.common.LinkedList
//import bps.ipr.common.LinkedQueue
//import bps.ipr.common.MutableIdentitySet
//import bps.ipr.common.Queue
//import bps.ipr.formulas.FolFormulaImplementation
//import bps.ipr.prover.tableau.BaseTableau
//import bps.ipr.prover.tableau.BaseTableauNode
//import bps.ipr.prover.tableau.Tableau
//import bps.ipr.prover.tableau.closing.DetailedFolCondensingBranchCloser
//import bps.ipr.prover.tableau.display.dot.DotDisplayTableauListener
//import bps.ipr.prover.tableau.rule.SignedFormula
//
//data class TempTreeNode(
//    val formulas: MutableIdentitySet<SignedFormula<*>> = MutableIdentitySet(),
//    var parent: TempTreeNode? = null,
//    var children: LinkedList<TempTreeNode>? = null,
//) {
//    val root: TempTreeNode get() = parent?.root ?: this
//}
//
//// NOTE ???could use the existing DotDisplayNodeListener but allow it to skip nodes and formulas not involved???
//fun DetailedFolCondensingBranchCloser.toTableau(formulaImplementation: FolFormulaImplementation): Tableau<BaseTableauNode, DetailedFolCondensingBranchCloser> {
//// TODO find all ancestors of involved formulas
//// TODO get the ones nearest the root
//    // TODO build a new tree from there including only involved formulas and nodes
//
//    val mapOriginalNodeIdToTempTreeNode = mutableMapOf<Long, TempTreeNode>()
//    // TODO each node has formulas
//    //      each formula has a parent
//    //      The max depth parent will determine the parent node.
//    //      The other parents will sit in a queue.
//    // TODO So, make a queue of signed formulas
//    val queue: Queue<SignedFormula<*>> =
//        LinkedQueue<SignedFormula<*>>()
//            .apply {
//                involved
//                    .forEach {
//                        enqueue(it)
//                    }
//            }
//    generateSequence { queue.dequeueOrNull() }
//        .forEach { signedFormula: SignedFormula<*> ->
//            mapOriginalNodeIdToTempTreeNode
//                .computeIfAbsent(signedFormula.birthPlace.id) { TempTreeNode() }
//                .let { leaf: TempTreeNode ->
//                    leaf.formulas.add(signedFormula)
//                }
//            signedFormula
//                .parentFormula
//                ?.let { queue.enqueue(it) }
//        }
//    // NOTE now mapOriginalNodeIdToTempTreeNode contains all involved formulas in a TreeNode
//    // TODO figure out tree structure:
//    //      - iterate through mapOriginalNodeIdToTempTreeNode by increasing key (these will give us extremities first)
//    //      - is the lowest parent of a formula here necessarily at the parent node?
//    // FIXME need to guarantee:
//    //   - all nodes are given their parents and children
//    // FIXME currently, I'm only looking at the lowest parent of formulas in each node.  However, since all the
//    //   ancestors of all the formulas are in the tree, all of them should be treated eventually
//    mapOriginalNodeIdToTempTreeNode
//        .keys
//        .sortedDescending()
//        .forEach { lowId ->
//            mapOriginalNodeIdToTempTreeNode[lowId]!!
//                .let { lowNode: TempTreeNode ->
//                    lowNode
//                        .formulas
//                        .maxBy { it.parentFormula?.birthPlace?.depth ?: 0 }
//                        .parentFormula
//                        ?.let { deepestParent: SignedFormula<*> ->
//                            deepestParent
//                                .birthPlace
//                                .id
//                                .let { parentId ->
//                                    mapOriginalNodeIdToTempTreeNode[parentId]!!
//                                        .let { parentNode ->
//                                            parentNode
//                                                .children
//                                                ?.let { children: LinkedList<TempTreeNode> ->
//                                                    children.add(lowNode)
//                                                }
//                                                ?: run { parentNode.children = LinkedList(lowNode) }
//                                            lowNode.parent = parentNode
//                                        }
//                                }
//                        }
//                }
//
//        }
//    val root: TempTreeNode = mapOriginalNodeIdToTempTreeNode.minBy { it.key }.value
//    return BaseTableau<DetailedFolCondensingBranchCloser>()
//        .apply {
//            setRootForFormulas(root.formulas)
//            DotDisplayTableauListener(tableau = this)
//                .also { tableauListener: DotDisplayTableauListener ->
//                    addAddNodeToTableauListener(tableauListener)
//                    addDisplayTableauListener(tableauListener)
//                }
//            FIXME fill in the rest of the tree
//        }
//
//}
//
