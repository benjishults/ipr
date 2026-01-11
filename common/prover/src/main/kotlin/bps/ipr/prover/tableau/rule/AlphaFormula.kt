package bps.ipr.prover.tableau.rule

import bps.ipr.common.ImpossibleError
import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.SignedFormula.Companion.create

sealed interface AlphaFormula<T : FolFormula> : SignedFormula<T> {
    // NOTE force children to implement this
    abstract override fun reduceAlpha(
        birthPlace: BaseTableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
        parent: SignedFormula<*>?,
    ): MutableList<SignedFormula<*>>

    override fun apply() =
        throw ImpossibleError("This should never be called due to the way addRule works.")
}

sealed interface SignedNotFormula : AlphaFormula<Not> {

    override fun reduceAlpha(
        birthPlace: BaseTableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
        parent: SignedFormula<*>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormula
            .let {
                create(
                    formula = it,
                    sign = !sign,
                    birthPlace = birthPlace,
                    formulaImplementation = formulaImplementation,
                    parentFormula = parent,
                )
                    .reduceAlpha(
                        birthPlace = birthPlace,
                        mutableList = mutableList,
                        parent = parent,
                    )
            }
}

data class PositiveNotFormula(
    override val formula: Not,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : SignedNotFormula, PositiveSignedFormula<Not>() {
    init {
        splits = computeSplits()
    }
}

data class NegativeNotFormula(
    override val formula: Not,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : SignedNotFormula, NegativeSignedFormula<Not>() {
    init {
        splits = computeSplits()
    }
}

data class NegativeImpliesFormula(
    override val formula: Implies,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : AlphaFormula<Implies>, NegativeSignedFormula<Implies>() {
    init {
        splits = computeSplits()
    }

    override fun reduceAlpha(
        birthPlace: BaseTableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
        parent: SignedFormula<*>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .let { implies ->
                create(implies.antecedent, true, birthPlace, formulaImplementation, parent)
                    .reduceAlpha(
                        birthPlace,
                        create(
                            formula = implies.consequent,
                            sign = false,
                            birthPlace = birthPlace,
                            formulaImplementation = formulaImplementation,
                            parentFormula = parent,
                        )
                            .reduceAlpha(
                                birthPlace = birthPlace,
                                parent = parent,
                                mutableList = mutableList,
                            ),
                        parent = parent,
                    )
            }

}

/**
 * Does the [reduceAlpha] work for [And] and [Or] [AlphaFormula]s.
 */
sealed interface SimpleMultiSubAlphaFormula<T : AbstractMultiFolFormula> : AlphaFormula<T> {
    override fun reduceAlpha(
        birthPlace: BaseTableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
        parent: SignedFormula<*>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormulas
            .fold(mutableList ?: mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                create(
                    formula = t,
                    sign = sign,
                    birthPlace = birthPlace,
                    formulaImplementation = formulaImplementation,
                    parentFormula = parent,
                )
                    .reduceAlpha(
                        birthPlace = birthPlace,
                        mutableList = r,
                        parent = parent,
                    )
                r
            }
}

data class NegativeOrFormula(
    override val formula: Or,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : NegativeSignedFormula<Or>(), SimpleMultiSubAlphaFormula<Or> {
    init {
        splits = computeSplits()
    }
}

data class PositiveAndFormula(
    override val formula: And,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : PositiveSignedFormula<And>(), SimpleMultiSubAlphaFormula<And> {
    init {
        splits = computeSplits()
    }
}
