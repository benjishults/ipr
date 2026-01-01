package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.prover.tableau.TableauNode
import bps.ipr.prover.tableau.rule.SignedFormula.Companion.create

sealed interface AlphaFormula<T : FolFormula> : SignedFormula<T> {
    // NOTE force children to implement this
    abstract override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>>

    override fun apply() =
        TODO("This should never be called due to the way addRule works.")
}

sealed interface SignedNotFormula : AlphaFormula<Not> {

    override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .subFormula
            .let {
                create(it, !sign, birthPlace, formulaImplementation)
                    .reduceAlpha(birthPlace, mutableList)
            }
}
data class PositiveNotFormula(
    override val formula: Not,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : SignedNotFormula, PositiveSignedFormula<Not>()

data class NegativeNotFormula(
    override val formula: Not,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : SignedNotFormula, NegativeSignedFormula<Not>()

data class NegativeImpliesFormula(
    override val formula: Implies,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : AlphaFormula<Implies>, NegativeSignedFormula<Implies>() {
    override fun reduceAlpha(
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        formula
            .let { implies ->
                create(implies.antecedent, true, birthPlace, formulaImplementation)
                    .reduceAlpha(
                        birthPlace,
                        create(implies.consequent, false, birthPlace, formulaImplementation)
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
                create(t, sign, birthPlace, formulaImplementation)
                    .reduceAlpha(birthPlace, r)
                r
            }
}

data class NegativeOrFormula(
    override val formula: Or,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : NegativeSignedFormula<Or>(), SimpleMultiSubAlphaFormula<Or>

data class PositiveAndFormula(
    override val formula: And,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : PositiveSignedFormula<And>(), SimpleMultiSubAlphaFormula<And>
