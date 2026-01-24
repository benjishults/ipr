package bps.ipr.prover.tableau.rule

import bps.ipr.common.Node
import bps.ipr.common.StringUtils
import bps.ipr.formulas.And
import bps.ipr.formulas.Falsity
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.Truth
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.CategorizedSignedFormulas.Companion.categorizeSignedFormulas
import kotlin.collections.forEach
import kotlin.math.min

/**
 * An inference rule that can be applied to a [bps.ipr.prover.tableau.BaseTableau].
 */
fun interface Rule {

    /**
     * applies the rule
     */
    fun apply()

}

sealed interface SignedFormula<T : FolFormula> : Rule {
    val formula: T
    val sign: Boolean
    val birthPlace: BaseTableauNode
    val formulaImplementation: FolFormulaImplementation
    // FIXME once we implement the epsilon rule, a formula will have multiple parents
    val parentFormula: SignedFormula<*>?
    val isSplitting: Boolean
        get() =
            parentFormula is BetaFormula<*>

    /**
     * If my parent was a Beta formula, then my birthplace's parent is the split node I depend on.
     * I also depend on all the splits of my parent.
     */
    val splits: Node<BaseTableauNode>?

    /**
     * Applies the rule for the given [formula] at its [birthPlace].  This is expected to add nodes to the tableau
     * under [birthPlace].
     */
    override fun apply()

    /**
     * @return the list of [SignedFormula]s that are the result of applying the alpha rule.
     * @param mutableList if not null, the result will be added to it instead of a new list being created.
     */
    fun reduceAlpha(
        birthPlace: BaseTableauNode,
        mutableList: MutableList<SignedFormula<*>>? = null,
        parent: SignedFormula<*>?,
    ): MutableList<SignedFormula<*>> =
        mutableList
            ?.apply {
                add(this@SignedFormula)
            }
            ?: mutableListOf(this)

    // TODO make these display functions work like others with a registry of different ways of doing it
    fun display( ) =
        formula.display(1 + birthPlace.depth)

    // TODO make these display functions work like others with a registry of different ways of doing it
    fun displayCompact(): String =
        with(StringUtils) {
            "${
                formula
                    .display()
                    // TODO consider making this decrease dependent on the number of splits above
                    .abbreviate(min(25, 100 / (birthPlace.depth + 1)))
            }\\l"
        }

    fun createNodeForReducedFormulas(
        leaf: BaseTableauNode,
        reducedFormulasFactory: (BaseTableauNode) -> List<SignedFormula<*>>,
    ): BaseTableauNode =
        BaseTableauNode(leaf)
            .also { node: BaseTableauNode ->
                birthPlace.tableau.registerNode(node)
                reducedFormulasFactory(node)
                    .also { reducedSignedFormulas: List<SignedFormula<*>> ->
                        val (pos, neg, closing, betas, deltas, gammas) = reducedSignedFormulas.categorizeSignedFormulas()
                        node.populate(
                            newAtomicHyps = pos,
                            newAtomicGoals = neg,
                            closing = closing,
                            betas = betas,
                            deltas = deltas,
                            gammas = gammas,
                        )
                    }
            }

    companion object {
        fun <F : FolFormula> create(
            formula: F,
            sign: Boolean,
            birthPlace: BaseTableauNode,
            formulaImplementation: FolFormulaImplementation,
            parentFormula: SignedFormula<*>?,
        ): SignedFormula<F> =
            (if (sign) {
                when (formula) {
                    is And -> PositiveAndFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Or -> PositiveOrFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Implies -> PositiveImpliesFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Iff -> PositiveIffFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is ForAll -> PositiveForAllFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is ForSome -> PositiveForSomeFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Not -> PositiveNotFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    Falsity -> PositiveClosingFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Predicate -> PositiveAtomicFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    Truth -> PositiveWastedFormula(formula, birthPlace, formulaImplementation, parentFormula)
                }
            } else {
                when (formula) {
                    is And -> NegativeAndFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Or -> NegativeOrFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Implies -> NegativeImpliesFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Iff -> NegativeIffFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is ForAll -> NegativeForAllFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is ForSome -> NegativeForSomeFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Not -> NegativeNotFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    Truth -> NegativeClosingFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    is Predicate -> NegativeAtomicFormula(formula, birthPlace, formulaImplementation, parentFormula)
                    Falsity -> NegativeWastedFormula(formula, birthPlace, formulaImplementation, parentFormula)
                }
            }) as SignedFormula<F>
    }
}

fun SignedFormula<*>.computeSplits(): Node<BaseTableauNode>? =
    if (isSplitting)
        Node(
            birthPlace.parent!!,
            parentFormula!!.splits,
        ) // FIXME can I improve this since all children will have the same splits?
    else
        parentFormula?.splits

data class CategorizedSignedFormulas(
    val positiveAtoms: List<PositiveAtomicFormula>?,
    val negativeAtoms: List<NegativeAtomicFormula>?,
    val closingFormulas: List<ClosingFormula<*>>?,
    val betas: List<BetaFormula<*>>?,
    val deltas: List<DeltaFormula<*>>?,
    val gammas: List<GammaFormula<*>>?,
) {
    companion object {
        fun Iterable<SignedFormula<*>>.categorizeSignedFormulas(): CategorizedSignedFormulas {
            var pos: MutableList<PositiveAtomicFormula>? = null
            var neg: MutableList<NegativeAtomicFormula>? = null
            var closing: MutableList<ClosingFormula<*>>? = null
            var betas: MutableList<BetaFormula<*>>? = null
            var deltas: MutableList<DeltaFormula<*>>? = null
            var gammas: MutableList<GammaFormula<*>>? = null
            forEach { signedFormula ->
                when (signedFormula) {
                    is PositiveAtomicFormula ->
                        pos?.add(signedFormula) ?: run { pos = mutableListOf(signedFormula) }
                    is NegativeAtomicFormula ->
                        neg?.add(signedFormula) ?: run { neg = mutableListOf(signedFormula) }
                    is ClosingFormula<*> ->
                        closing?.add(signedFormula) ?: run { closing = mutableListOf(signedFormula) }
                    is BetaFormula<*> ->
                        betas?.add(signedFormula) ?: run { betas = mutableListOf(signedFormula) }
                    is DeltaFormula<*> ->
                        deltas?.add(signedFormula) ?: run { deltas = mutableListOf(signedFormula) }
                    is GammaFormula<*> ->
                        gammas?.add(signedFormula) ?: run { gammas = mutableListOf(signedFormula) }
                    else ->
                        Unit
                }
            }
            return CategorizedSignedFormulas(pos, neg, closing, betas, deltas, gammas)
        }
    }
}

sealed class PositiveSignedFormula<F : FolFormula> : SignedFormula<F> {
    override val sign: Boolean = true
    final override var splits: Node<BaseTableauNode>? = null
        protected set
}

sealed class NegativeSignedFormula<F : FolFormula> : SignedFormula<F> {
    override val sign: Boolean = false
    final override var splits: Node<BaseTableauNode>? = null
        protected set
}
