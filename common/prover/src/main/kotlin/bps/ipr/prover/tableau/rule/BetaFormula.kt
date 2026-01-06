package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Or
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.prover.tableau.rule.SignedFormula.Companion.create
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
                            createNodeForReducedFormulas { node: BaseTableauNode ->
                                create(
                                    formula = folFormula,
                                    sign = sign,
                                    birthPlace = node,
                                    formulaImplementation = formulaImplementation,
                                    parent = this,
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
) : SimpleMultiSubBetaFormula<Or>, PositiveSignedFormula<Or>()

data class NegativeAndFormula(
    override val formula: And,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : SimpleMultiSubBetaFormula<And>, NegativeSignedFormula<And>()

data class PositiveImpliesFormula(
    override val formula: Implies,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : BetaFormula<Implies>, PositiveSignedFormula<Implies>() {

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas { node: BaseTableauNode ->
                            create(
                                formula = formula.consequent,
                                sign = true,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parent = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    parent = this
                                )
                        },
                        createNodeForReducedFormulas { node: BaseTableauNode ->
                            create(
                                formula = formula.antecedent,
                                sign = false,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parent = this,
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

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas { node: BaseTableauNode ->
                            create(
                                formula = formula.subFormulas[0],
                                sign = false,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parent = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        create(
                                            formula = formula.subFormulas[1],
                                            sign = true,
                                            birthPlace = node,
                                            formulaImplementation = formulaImplementation,
                                            parent = this,
                                        )
                                            .reduceAlpha(
                                                birthPlace = node,
                                                parent = this
                                            ),
                                    parent = this,
                                )
                        },
                        createNodeForReducedFormulas { node: BaseTableauNode ->
                            create(
                                formula = formula.subFormulas[0],
                                sign = true,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                                parent = this,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        create(
                                            formula = formula.subFormulas[1],
                                            sign = false,
                                            birthPlace = node,
                                            formulaImplementation = formulaImplementation,
                                            parent = this,
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

    override fun apply() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                leaf.setChildren(
                    listOf(
                        createNodeForReducedFormulas { node: BaseTableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    create(
                                        formula = t,
                                        sign = true,
                                        birthPlace = node,
                                        formulaImplementation = formulaImplementation,
                                        parent = this
                                    )
                                        .reduceAlpha(
                                            birthPlace = node,
                                            mutableList = r,
                                            parent = this,
                                        )
                                    r
                                }
                        },
                        createNodeForReducedFormulas { node: BaseTableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    create(
                                        formula = t,
                                        sign = false,
                                        birthPlace = node,
                                        formulaImplementation = formulaImplementation,
                                        parent = this
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
