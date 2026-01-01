package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.And
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Or
import bps.ipr.prover.tableau.TableauNode
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
                                    formulaImplementation = formulaImplementation,
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
    override val formulaImplementation: FolFormulaImplementation,
) : SimpleMultiSubBetaFormula<Or>, PositiveSignedFormula<Or>()

data class NegativeAndFormula(
    override val formula: And,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : SimpleMultiSubBetaFormula<And>, NegativeSignedFormula<And>()

data class PositiveImpliesFormula(
    override val formula: Implies,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
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
                                formulaImplementation = formulaImplementation,
                            )
                                .reduceAlpha(birthPlace = node)
                        },
                        createNodeForReducedFormulas { node: TableauNode ->
                            create(
                                formula = formula.antecedent,
                                sign = false,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
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
    override val formulaImplementation: FolFormulaImplementation,
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
                                formulaImplementation = formulaImplementation,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        create(
                                            formula = formula.subFormulas[1],
                                            sign = true,
                                            birthPlace = node,
                                            formulaImplementation = formulaImplementation,
                                        )
                                            .reduceAlpha(birthPlace = node),
                                )
                        },
                        createNodeForReducedFormulas { node: TableauNode ->
                            create(
                                formula = formula.subFormulas[0],
                                sign = true,
                                birthPlace = node,
                                formulaImplementation = formulaImplementation,
                            )
                                .reduceAlpha(
                                    birthPlace = node,
                                    mutableList =
                                        create(
                                            formula = formula.subFormulas[1],
                                            sign = false,
                                            birthPlace = node,
                                            formulaImplementation = formulaImplementation,
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
    override val formulaImplementation: FolFormulaImplementation,
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
                                    create(t, true, node, formulaImplementation)
                                        .reduceAlpha(node, r)
                                    r
                                }
                        },
                        createNodeForReducedFormulas { node: TableauNode ->
                            formula
                                .subFormulas
                                // NOTE this generates less garbage than the flatMap
                                .fold(mutableListOf()) { r: MutableList<SignedFormula<*>>, t: FolFormula ->
                                    create(t, false, node, formulaImplementation)
                                        .reduceAlpha(node, r)
                                    r
                                }
                        },
                    ),
                )
            }

}
