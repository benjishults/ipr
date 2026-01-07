package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Or
import bps.ipr.prover.tableau.BaseTableauNode
import kotlin.collections.forEach

sealed interface BetaFormula<T : FolFormula> : SignedFormula<T>

/**
 * Does the [apply] work for [And] and [Or] [BetaFormula]s.
 */
sealed interface SimpleMultiSubBetaFormula<T : AbstractMultiFolFormula> : BetaFormula<T> {
    override fun apply() {
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    formula
                        .subFormulas
                        .map { folFormula: FolFormula ->
                            createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                                SignedFormula.create(
                                    formula = folFormula,
                                    sign = sign,
                                    birthPlace = node,
                                    formulaImplementation = formulaImplementation,
                                    parentFormula = this,
                                )
                                    .reduceAlpha(
                                        birthPlace = node,
                                        parent = this,
                                    )
                            }
                        },
                )
            }
    }
}

data class PositiveOrFormula(
    override val formula: Or,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : SimpleMultiSubBetaFormula<Or>, PositiveSignedFormula<Or>() {
    init {
        splits = computeSplits()
    }
}

data class NegativeAndFormula(
    override val formula: And,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : SimpleMultiSubBetaFormula<And>, NegativeSignedFormula<And>() {
    init {
        splits = computeSplits()
    }
}

data class PositiveImpliesFormula(
    override val formula: Implies,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : BetaFormula<Implies>, PositiveSignedFormula<Implies>() {

    init {
        splits = computeSplits()
    }

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                            SignedFormula.create(
                                formula = formula.consequent,
                                sign = true,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parentFormula = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    parent = this
                                )
                        },
                        createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                            SignedFormula.create(
                                formula = formula.antecedent,
                                sign = false,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parentFormula = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    parent = this
                                )
                        },
                    ),
                )
            }

}

data class NegativeIffFormula(
    override val formula: Iff,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : BetaFormula<Iff>, NegativeSignedFormula<Iff>() {

    init {
        splits = computeSplits()
    }

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                            SignedFormula.create(
                                formula = formula.subFormulas[0],
                                sign = false,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parentFormula = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        SignedFormula.create(
                                            formula = formula.subFormulas[1],
                                            sign = true,
                                            birthPlace = node,
                                            formulaImplementation = formulaImplementation,
                                            parentFormula = this,
                                        )
                                            .reduceAlpha(
                                                birthPlace = node,
                                                parent = this
                                            ),
                                    parent = this,
                                )
                        },
                        createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                            SignedFormula.create(
                                formula = formula.subFormulas[0],
                                sign = true,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parentFormula = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        SignedFormula.create(
                                            formula = formula.subFormulas[1],
                                            sign = false,
                                            birthPlace = node,
                                            formulaImplementation = formulaImplementation,
                                            parentFormula = this,
                                        )
                                            .reduceAlpha(
                                                birthPlace = node,
                                                parent = this
                                            ),
                                    parent = this,
                                )
                        },
                    ),
                )
            }

}

data class PositiveIffFormula(
    override val formula: Iff,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : BetaFormula<Iff>, PositiveSignedFormula<Iff>() {

    init {
        splits = computeSplits()
    }

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    SignedFormula.create(
                                        formula = t,
                                        sign = true,
                                        birthPlace = node,
                                        formulaImplementation = formulaImplementation,
                                        parentFormula = this
                                    )
                                        .reduceAlpha(
                                            birthPlace = node,
                                            mutableList = r,
                                            parent = this,
                                        )
                                    r
                                }
                        },
                        createNodeForReducedFormulas(leaf) { node: BaseTableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    SignedFormula.create(
                                        formula = t,
                                        sign = false,
                                        birthPlace = node,
                                        formulaImplementation = formulaImplementation,
                                        parentFormula = this
                                    )
                                        .reduceAlpha(
                                            birthPlace = node,
                                            mutableList = r,
                                            parent = this,
                                        )
                                    r
                                }
                        },
                    ),
                )
            }

}
