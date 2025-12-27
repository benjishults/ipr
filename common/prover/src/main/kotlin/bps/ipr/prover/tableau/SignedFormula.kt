package bps.ipr.prover.tableau

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Falsity
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.Truth

sealed interface SignedFormula<T : FolFormula> {
    val formula: T
    val sign: Boolean
    val birthPlace: TableauNode

    fun apply()

    /**
     * @return the list of [SignedFormula]s that are the result of applying the alpha rule.
     * @param mutableList if not null, the result will be added to it instead of a new list being created.
     */
    fun reduce(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>? = null,
    ): MutableList<SignedFormula<*>> =
        mutableList
            ?.apply {
                add(this@SignedFormula)
            }
            ?: mutableListOf(this)


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
        Unit

    /**
     * Deletes itself.
     */
    override fun reduce(
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
    override fun apply() = Unit
}

data class NegativeAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: TableauNode,
) : AtomicSignedFormula, NegativeSignedFormula<Predicate>() {
    override fun apply() = Unit
}

data class PositiveAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: TableauNode,
) : AtomicSignedFormula, PositiveSignedFormula<Predicate>() {
    override fun apply() = Unit
}

sealed interface AlphaFormula<T : FolFormula> : SignedFormula<T> {
    // NOTE force children to implement this
    abstract override fun reduce(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>>
}

//fun List<SignedFormula<*>>.reduce(parent: TableauNode): List<SignedFormula<*>> =
//    flatMap { signedFormula: SignedFormula<*, *> ->
//        signedFormula.rule
//            .let {
//                when (it) {
//                    is AlphaRule<*> -> it.reduce(signedFormula, parent)
//                    else -> listOf(signedFormula)
//                }
//            }
//
//    }

fun List<SignedFormula<*>>.extractBySign(): Pair<List<PositiveSignedFormula<*>>, List<NegativeSignedFormula<*>>> =
    mutableListOf<PositiveSignedFormula<*>>()
        .let { pos ->
            mutableListOf<NegativeSignedFormula<*>>()
                .let { neg ->
                    forEach { signedFormula ->
                        when (signedFormula) {
                            is PositiveSignedFormula<*> -> pos.add(signedFormula)
                            is NegativeSignedFormula<*> -> neg.add(signedFormula)
                        }
                    }
                    pos to neg
                }
        }

sealed interface SignedNotFormula : AlphaFormula<Not> {
    override fun apply() =
        birthPlace
            // TODO figure out splicing
            .findLeaves()
            .forEach { parent: TableauNode ->
                listOf(SignedFormula.create(formula.subFormula, !sign, parent))
                    // TODO take advantage of the fact that formulas are immutable
                    //      make those first and copy references between the various SignedFormulas
                    .flatMap { it.reduce(parent) }
                    .let { newSignedFormulas: List<SignedFormula<*>> ->
                        newSignedFormulas
                            .forEach {
                                parent
                                    .tableau
                                    .applicableRules
                                    .addRule(it)
                            }
                        parent
                            .children
                            .takeIf { it.isEmpty() }
                            ?.let {
                                val (pos, neg) = newSignedFormulas.extractBySign()
                                parent.children =
                                    listOf(
                                        TableauNode(
                                            tableau = parent.tableau,
                                            parent = parent,
                                            newHyps = pos,
                                            newGoals = neg,
                                        ),
                                    )
                            }
                            ?: throw RuntimeException("Expected to be looking at a childless node")
                    }
            }

    override fun reduce(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormula
            .let {
                SignedFormula.create(it, !sign, birthPlace)
                    .reduce(birthPlace, mutableList)
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
) : SignedNotFormula, PositiveSignedFormula<Not>() {
    override fun apply() {
        TODO("Not yet implemented")
    }

}

data class NegativeNotFormula(
    override val formula: Not,
    override val birthPlace: TableauNode,
) : SignedNotFormula, NegativeSignedFormula<Not>() {
    override fun apply() {
        // TODO make a child with a Positive of the arg
        TODO("Not yet implemented")
    }
}

data class NegativeImpliesFormula(
    override val formula: Implies,
    override val birthPlace: TableauNode,
) : AlphaFormula<Implies>, NegativeSignedFormula<Implies>() {
    override fun reduce(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .let { implies ->
                SignedFormula
                    .create(implies.antecedent, false, birthPlace)
                    .reduce(
                        birthPlace,
                        SignedFormula
                            .create(implies.consequent, true, birthPlace)
                            .reduce(birthPlace, mutableList),
                    )
            }

    override fun apply() {
        TODO("Not yet implemented")
    }

}

/**
 * Does the [reduce] work for [And] and [Or] [AlphaFormula]s.
 */
sealed interface SimpleMultiSubAlphaFormula<T : AbstractMultiFolFormula> : AlphaFormula<T> {
    override fun reduce(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormulas
            .fold(mutableList ?: mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                SignedFormula.create(t, sign, birthPlace)
                    .reduce(birthPlace, r)
                r
            }
}

data class NegativeOrFormula(
    override val formula: Or,
    override val birthPlace: TableauNode,
) : NegativeSignedFormula<Or>(), SimpleMultiSubAlphaFormula<Or> {

    override fun apply() {
        TODO()
    }

}

data class PositiveAndFormula(
    override val formula: And,
    override val birthPlace: TableauNode,
) : PositiveSignedFormula<And>(), SimpleMultiSubAlphaFormula<And> {

    override fun apply() {
        TODO("Not yet implemented")
    }

}

sealed interface BetaFormula<T : FolFormula> : SignedFormula<T>

data class PositiveOrFormula(
    override val formula: Or,
    override val birthPlace: TableauNode,
) : BetaFormula<Or>, PositiveSignedFormula<Or>() {
    override fun apply(): Unit {
//        formula.subFormulas.map { SignedFormula.create(it, true, birthPlace) }
        TODO()
    }
}

data class PositiveImpliesFormula(
    override val formula: Implies,
    override val birthPlace: TableauNode,
) : BetaFormula<Implies>, PositiveSignedFormula<Implies>() {
    override fun apply() {
        TODO()
//        listOf(
//            SignedFormula(formula.antecedent, false, birthPlace),
//            SignedFormula(formula.consequent, true, birthPlace),
//        )
    }
}

data class NegativeAndFormula(
    override val formula: And,
    override val birthPlace: TableauNode,
) : BetaFormula<And>, NegativeSignedFormula<And>() {
    override fun apply() {
//        formula.subFormulas.map { SignedFormula(it, false, birthPlace) }
        TODO()
    }
}

data class NegativeIffFormula(
    override val formula: Iff,
    override val birthPlace: TableauNode,
) : BetaFormula<Iff>, NegativeSignedFormula<Iff>() {
    override fun apply() = TODO()

}

data class PositiveIffFormula(
    override val formula: Iff,
    override val birthPlace: TableauNode,
) : BetaFormula<Iff>, PositiveSignedFormula<Iff>() {
    override fun apply() {
        TODO("Not yet implemented")
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

sealed interface GammaFormula<T : FolFormula> : SignedFormula<T>

data class NegativeForSomeFormula(
    override val formula: ForSome,
    override val birthPlace: TableauNode,
) : GammaFormula<ForSome>, NegativeSignedFormula<ForSome>() {
    override fun apply() {
        TODO("Not yet implemented")
    }
}

data class PositiveForAllFormula(
    override val formula: ForAll,
    override val birthPlace: TableauNode,
) : GammaFormula<ForAll>, PositiveSignedFormula<ForAll>() {
    override fun apply() {
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
