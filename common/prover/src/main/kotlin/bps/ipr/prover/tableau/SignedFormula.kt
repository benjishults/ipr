package bps.ipr.prover.tableau

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.Falsity
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.Truth
import bps.ipr.prover.tableau.SignedFormula.Companion.create

sealed interface SignedFormula<T : FolFormula> {
    val formula: T
    val sign: Boolean
    val birthPlace: TableauNode

    /**
     * Applies the rule for the given [formula] at its [birthPlace].  This is expected to add nodes to the tableau
     * under [birthPlace].
     */
    fun apply()

    /**
     * @return the list of [SignedFormula]s that are the result of applying the alpha rule.
     * @param mutableList if not null, the result will be added to it instead of a new list being created.
     */
    fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>? = null,
    ): MutableList<SignedFormula<*>> =
        mutableList
            ?.apply {
                add(this@SignedFormula)
            }
            ?: mutableListOf(this)

    fun display(i: Int) = buildString {
        append(" ".repeat(i))
        append(formula.display())
    }

    fun createNodeForReducedFormulas(
        reducedFormulasFactory: (TableauNode) -> List<SignedFormula<*>>,
    ): TableauNode =
        TableauNode()
            .also { node: TableauNode ->
                birthPlace.tableau.registerNode(node)
                reducedFormulasFactory(node)
                    .also { reducedSignedFormulas: List<SignedFormula<*>> ->
                        val (pos, neg, closing, betas, deltas, gammas) = CategorizedSignedFormulas(reducedSignedFormulas)
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
            birthPlace: TableauNode,
        ): SignedFormula<F> =
            (if (sign) {
                when (formula) {
                    is And -> PositiveAndFormula(formula, birthPlace)
                    is Or -> PositiveOrFormula(formula, birthPlace)
                    is Implies -> PositiveImpliesFormula(formula, birthPlace)
                    is Iff -> PositiveIffFormula(formula, birthPlace)
                    is ForAll -> PositiveForAllFormula(formula, birthPlace)
                    is ForSome -> PositiveForSomeFormula(formula, birthPlace)
                    is Not -> PositiveNotFormula(formula, birthPlace)
                    Falsity -> PositiveClosingFormula(formula, birthPlace)
                    is Predicate -> PositiveAtomicFormula(formula, birthPlace)
                    Truth -> PositiveWastedFormula(formula, birthPlace)
                }
            } else {
                when (formula) {
                    is And -> NegativeAndFormula(formula, birthPlace)
                    is Or -> NegativeOrFormula(formula, birthPlace)
                    is Implies -> NegativeImpliesFormula(formula, birthPlace)
                    is Iff -> NegativeIffFormula(formula, birthPlace)
                    is ForAll -> NegativeForAllFormula(formula, birthPlace)
                    is ForSome -> NegativeForSomeFormula(formula, birthPlace)
                    is Not -> NegativeNotFormula(formula, birthPlace)
                    Truth -> NegativeClosingFormula(formula, birthPlace)
                    is Predicate -> NegativeAtomicFormula(formula, birthPlace)
                    Falsity -> NegativeWastedFormula(formula, birthPlace)
                }
            }) as SignedFormula<F>
    }
}

// should this be an alpha?
sealed interface WastedSignedFormula<F : FolFormula> : SignedFormula<F> {
    /**
     * Does nothing.
     */
    override fun apply() =
        TODO("should never be called due to the way addRule works")

    /**
     * Deletes itself.
     */
    override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        mutableList
            ?: mutableListOf()
}

data class NegativeWastedFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
) : WastedSignedFormula<FolFormula>, NegativeSignedFormula<FolFormula>() {}

data class PositiveWastedFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
) : WastedSignedFormula<FolFormula>, PositiveSignedFormula<FolFormula>() {}

sealed interface AtomicSignedFormula : SignedFormula<Predicate> {
    override fun apply() =
        TODO("should never be called due to the way addRule works")
}

data class NegativeAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: TableauNode,
) : AtomicSignedFormula, NegativeSignedFormula<Predicate>() {
}

data class PositiveAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: TableauNode,
) : AtomicSignedFormula, PositiveSignedFormula<Predicate>() {
}

sealed interface AlphaFormula<T : FolFormula> : SignedFormula<T> {
    // NOTE force children to implement this
    abstract override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>>

    override fun apply() =
        TODO("This should never be called due to the way addRule works.")
}

data class CategorizedSignedFormulas(
    val positiveAtoms: List<PositiveAtomicFormula>,
    val negativeAtoms: List<NegativeAtomicFormula>,
    val closingFormulas: List<ClosingFormula<*>>,
    val betas: List<BetaFormula<*>>,
    val deltas: List<DeltaFormula<*>>,
    val gammas: List<GammaFormula<*>>,
) {
    companion object {
        operator fun invoke(signedFormulas: List<SignedFormula<*>>): CategorizedSignedFormulas {
            val pos = mutableListOf<PositiveAtomicFormula>()
            val neg = mutableListOf<NegativeAtomicFormula>()
            val closing = mutableListOf<ClosingFormula<*>>()
            val betas = mutableListOf<BetaFormula<*>>()
            val deltas = mutableListOf<DeltaFormula<*>>()
            val gammas = mutableListOf<GammaFormula<*>>()
            signedFormulas
                .forEach { signedFormula ->
                    when (signedFormula) {
                        is PositiveAtomicFormula ->
                            pos.add(signedFormula)
                        is NegativeAtomicFormula ->
                            neg.add(signedFormula)
                        is ClosingFormula<*> ->
                            closing.add(signedFormula)
                        is BetaFormula<*> ->
                            betas.add(signedFormula)
                        is DeltaFormula<*> ->
                            deltas.add(signedFormula)
                        is GammaFormula<*> ->
                            gammas.add(signedFormula)
                        else ->
                            Unit
                    }
                }
            return CategorizedSignedFormulas(pos, neg, closing, betas, deltas, gammas)
        }
    }
}

sealed interface SignedNotFormula : AlphaFormula<Not> {

    override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormula
            .let {
                create(it, !sign, birthPlace)
                    .reduceAlpha(birthPlace, mutableList)
            }
}

sealed class PositiveSignedFormula<F : FolFormula> : SignedFormula<F> {
    override val sign: Boolean = true
}

sealed class NegativeSignedFormula<F : FolFormula> : SignedFormula<F> {
    override val sign: Boolean = false
}

data class PositiveNotFormula(
    override val formula: Not,
    override val birthPlace: TableauNode,
) : SignedNotFormula, PositiveSignedFormula<Not>()

data class NegativeNotFormula(
    override val formula: Not,
    override val birthPlace: TableauNode,
) : SignedNotFormula, NegativeSignedFormula<Not>()

data class NegativeImpliesFormula(
    override val formula: Implies,
    override val birthPlace: TableauNode,
) : AlphaFormula<Implies>, NegativeSignedFormula<Implies>() {
    override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .let { implies ->
                create(implies.antecedent, true, birthPlace)
                    .reduceAlpha(
                        birthPlace,
                        create(implies.consequent, false, birthPlace)
                            .reduceAlpha(birthPlace, mutableList),
                    )
            }

}

/**
 * Does the [reduceAlpha] work for [And] and [Or] [AlphaFormula]s.
 */
sealed interface SimpleMultiSubAlphaFormula<T : AbstractMultiFolFormula> : AlphaFormula<T> {
    override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormulas
            .fold(mutableList ?: mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                create(t, sign, birthPlace)
                    .reduceAlpha(birthPlace, r)
                r
            }
}

data class NegativeOrFormula(
    override val formula: Or,
    override val birthPlace: TableauNode,
) : NegativeSignedFormula<Or>(), SimpleMultiSubAlphaFormula<Or>

data class PositiveAndFormula(
    override val formula: And,
    override val birthPlace: TableauNode,
) : PositiveSignedFormula<And>(), SimpleMultiSubAlphaFormula<And>

sealed interface BetaFormula<T : FolFormula> : SignedFormula<T>

/**
 * Does the [apply] work for [And] and [Or] [BetaFormula]s.
 */
sealed interface SimpleMultiSubBetaFormula<T : AbstractMultiFolFormula> : BetaFormula<T> {
    override fun apply() {
        birthPlace
            .leaves()
            .forEach { leaf: TableauNode ->
                leaf.setChildren(
                    formula
                        .subFormulas
                        .map { folFormula: FolFormula ->
                            createNodeForReducedFormulas { node: TableauNode ->
                                create(
                                    formula = folFormula,
                                    sign = sign,
                                    birthPlace = node,
                                )
                                    .reduceAlpha(birthPlace = node)
                            }
                        },
                )
            }
    }
}

data class PositiveOrFormula(
    override val formula: Or,
    override val birthPlace: TableauNode,
) : SimpleMultiSubBetaFormula<Or>, PositiveSignedFormula<Or>()

data class NegativeAndFormula(
    override val formula: And,
    override val birthPlace: TableauNode,
) : SimpleMultiSubBetaFormula<And>, NegativeSignedFormula<And>()

data class PositiveImpliesFormula(
    override val formula: Implies,
    override val birthPlace: TableauNode,
) : BetaFormula<Implies>, PositiveSignedFormula<Implies>() {

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: TableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas { node: TableauNode ->
                            create(
                                formula = formula.consequent,
                                sign = true,
                                birthPlace = node,
                            )
                                .reduceAlpha(birthPlace = node)
                        },
                        createNodeForReducedFormulas { node: TableauNode ->
                            create(
                                formula = formula.antecedent,
                                sign = false,
                                birthPlace = node,
                            )
                                .reduceAlpha(birthPlace = node)
                        },
                    ),
                )
            }

}

data class NegativeIffFormula(
    override val formula: Iff,
    override val birthPlace: TableauNode,
) : BetaFormula<Iff>, NegativeSignedFormula<Iff>() {
    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: TableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas { node: TableauNode ->
                            create(
                                formula = formula.subFormulas[0],
                                sign = false,
                                birthPlace = node,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        create(
                                            formula = formula.subFormulas[1],
                                            sign = true,
                                            birthPlace = node,
                                        )
                                            .reduceAlpha(birthPlace = node),
                                )
                        },
                        createNodeForReducedFormulas { node: TableauNode ->
                            create(
                                formula = formula.subFormulas[0],
                                sign = true,
                                birthPlace = node,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        create(
                                            formula = formula.subFormulas[1],
                                            sign = false,
                                            birthPlace = node,
                                        )
                                            .reduceAlpha(birthPlace = node),
                                )
                        },
                    ),
                )
            }

}

data class PositiveIffFormula(
    override val formula: Iff,
    override val birthPlace: TableauNode,
) : BetaFormula<Iff>, PositiveSignedFormula<Iff>() {
    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: TableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas { node: TableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    create(t, true, node)
                                        .reduceAlpha(node, r)
                                    r
                                }
                        },
                        createNodeForReducedFormulas { node: TableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    create(t, false, node)
                                        .reduceAlpha(node, r)
                                    r
                                }
                        },
                    ),
                )
            }
}

sealed interface DeltaFormula<T : FolFormula> : SignedFormula<T>

data class NegativeForAllFormula(
    override val formula: ForAll,
    override val birthPlace: TableauNode,
) : DeltaFormula<ForAll>, NegativeSignedFormula<ForAll>() {
    override fun apply() {
        TODO("Not yet implemented")
    }
}

data class PositiveForSomeFormula(
    override val formula: ForSome,
    override val birthPlace: TableauNode,
) : DeltaFormula<ForSome>, PositiveSignedFormula<ForSome>() {
    override fun apply() {
        TODO("Not yet implemented")
    }
}

sealed interface GammaFormula<T : FolFormula> : SignedFormula<T> {
    var applications: Int

    fun applyGammaRule()

    override fun apply() =
        applyGammaRule()
            .also {
                applications++
                birthPlace
                    .tableau
                    .applicableRules
                    .addRule(this)
            }
}

data class NegativeForSomeFormula(
    override val formula: ForSome,
    override val birthPlace: TableauNode,
) : GammaFormula<ForSome>, NegativeSignedFormula<ForSome>() {

    override var applications: Int = 0

    override fun applyGammaRule() {
        TODO("Not yet implemented")
    }
}

data class PositiveForAllFormula(
    override val formula: ForAll,
    override val birthPlace: TableauNode,
) : GammaFormula<ForAll>, PositiveSignedFormula<ForAll>() {

    override var applications: Int = 0

    override fun applyGammaRule() {
        TODO("Not yet implemented")
    }
}

sealed interface ClosingFormula<F : FolFormula> : SignedFormula<F> {
    override fun apply() = Unit
}

data class NegativeClosingFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
) : ClosingFormula<FolFormula>, NegativeSignedFormula<FolFormula>() {
    override fun apply() = Unit
}

data class PositiveClosingFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
) : ClosingFormula<FolFormula>, PositiveSignedFormula<FolFormula>() {
    override fun apply() = Unit
}
