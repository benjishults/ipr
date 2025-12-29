package bps.ipr.prover.tableau

import bps.ipr.common.Queue
import bps.ipr.common.queue
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofSuccess
import bps.ipr.terms.EmptySubstitution
import bps.ipr.terms.IdempotentSubstitution

class Tableau {

    private var _root: TableauNode? = null
    var root: TableauNode
        get() = _root!!
        set(value) {
            if (_root === null) {
                _root = value
                registerNode(value)
            } else
                throw IllegalStateException("Root already set")
        }
    val applicableRules: RuleSet = RuleSet()

//    private val nodeToStateMap: MutableMap<Long, NodeState> = mutableMapOf()

    private var _size: Long = 0
    val size: Long
        get() = _size

    fun registerNode(node: TableauNode) {
        node.tableau = this
//        nodeToStateMap[node.id] = NodeState(node)
    }

    data class TreeCloserBuilder(
        var runningSubstitution: IdempotentSubstitution? = null,
    )

    fun attemptClose(unifier: FormulaUnifier): FolProofSuccess? {
        return root
            .attemptClose(
                queue = queue(),
                substitution = null,
                positiveAtomsAbove = emptyList(),
                negativeAtomsAbove = emptyList(),
                formulaUnifier = unifier,
            )
            ?.let { FolProofSuccess(it) }
    }

    /**
     * @return null if there is no substitution under [substitution] that closes this node and the rest of the nodes
     * in [queue].  Otherwise, returns a substitution under [substitution] that closes this node and the rest of the nodes
     * in [queue].
     * @param substitution `null` means that no node has been closed so far so any substitution is valid.
     * @param queue the rest of the nodes in the tree to be closed.  (breadth-first traversal)
     */
    private fun TableauNode.attemptClose(
        queue: Queue<TableauNode>,
        substitution: IdempotentSubstitution?,
        positiveAtomsAbove: List<PositiveAtomicFormula>,
        negativeAtomsAbove: List<NegativeAtomicFormula>,
        formulaUnifier: FormulaUnifier,
    ): IdempotentSubstitution? {
        if (closables.isNotEmpty()) {
            return substitution ?: EmptySubstitution
        } else {
            sequenceOfUnifiersHere(positiveAtomsAbove, negativeAtomsAbove, substitution, formulaUnifier)
                .forEach { unifier: IdempotentSubstitution ->
                    if (unifier === EmptySubstitution) {
                        return EmptySubstitution
                    } else {
                        // NOTE unifiers cannot be empty from this point
                        (queue.dequeueOrNull() ?: return unifier)
                            .attemptClose(
                                queue = queue,
                                substitution = unifier,
                                positiveAtomsAbove = positiveAtomsAbove + newAtomicHyps,
                                negativeAtomsAbove = negativeAtomsAbove + newAtomicGoals,
                                formulaUnifier = formulaUnifier,
                            )
                            ?.let { nonEmptyUnifier: IdempotentSubstitution ->
                                return nonEmptyUnifier
                            }
                        // NOTE If attemptClose returned null, then there's no way to close the rest of the nodes in
                        //  the tree under unifier.  So, we just move on.
                    }
                }


        }
//        node.newAtomicGoals.forEach { goals = goals.addToBeginning(it) }

        // TODO check for closing formulas
        // TODO unify
        // TODO
        return null
    }

    private fun TableauNode.sequenceOfUnifiersHere(
        positiveAtomsAbove: List<PositiveAtomicFormula>,
        negativeAtomsAbove: List<NegativeAtomicFormula>,
        substitution: IdempotentSubstitution?,
        formulaUnifier: FormulaUnifier,
    ): Sequence<IdempotentSubstitution> =
        newAtomicHyps
            .asSequence()
            .flatMap { newHyp: PositiveAtomicFormula ->
                (negativeAtomsAbove.asSequence() + newAtomicGoals.asSequence())
                    .mapNotNull { goalAbove: NegativeAtomicFormula ->
                        formulaUnifier.unify(
                            formula1 = newHyp.formula,
                            formula2 = goalAbove.formula,
                            under = substitution ?: EmptySubstitution,
                        )
                    }
            } +
                newAtomicGoals
                    .asSequence()
                    .flatMap { newGoal: NegativeAtomicFormula ->
                        positiveAtomsAbove
                            .asSequence()
                            .mapNotNull { hypAbove: PositiveAtomicFormula ->
                                formulaUnifier.unify(
                                    formula1 = newGoal.formula,
                                    formula2 = hypAbove.formula,
                                    under = substitution ?: EmptySubstitution,
                                )
                            }
                    }

    fun display(): String =
        buildString {
            root.breadthFirstTraverse { append(it.display(it.depth())) }
        }

    companion object {
        // NOTE had to do this outside a constructor because I have to have the generic function
        operator fun <T : FolFormula> invoke(formula: T): Tableau {
            return Tableau()
                .also { tableau: Tableau ->
                    TableauNode()
                        .also { root: TableauNode ->
                            tableau.root = root
                            SignedFormula
                                .create(
                                    formula = formula,
                                    sign = false,
                                    birthPlace = root,
                                )
                                .reduce(birthPlace = root)
                                .also {
                                    val (pos, neg, closing, betas, deltas, gammas) = CategorizedSignedFormulas(it)
                                    root.populate(
                                        newAtomicHyps = pos,
                                        newAtomicGoals = neg,
                                        closing = closing,
                                        betas = betas,
                                        deltas = deltas,
                                        gammas = gammas,
                                    )
//                                root.newGoals = neg
                                }
//                        .forEach { signedFormula ->
//                            applicableRules.addRule(signedFormula)
//                        }
                        }
                }
        }
    }

}
