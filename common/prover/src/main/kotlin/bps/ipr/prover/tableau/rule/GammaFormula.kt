package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.VariablesBindingFolFormula
import bps.ipr.prover.tableau.BaseTableauNode
import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.substitution.SingletonIdempotentSubstitution
import bps.ipr.terms.Variable
import kotlin.collections.forEach

sealed interface GammaFormula<T : VariablesBindingFolFormula> : SignedFormula<T> {
    var applications: Int

    fun applyGammaRule() =
        birthPlace
            .leaves()
            .forEach { leaf: BaseTableauNode ->
                // NOTE we want fresh free variables on each branch
                createGammaChild()
                    .let { childFormula: FolFormula ->
                        leaf.setChildren(
                            listOf(
                                createNodeForReducedFormulas { node: BaseTableauNode ->
                                    SignedFormula.create(childFormula, sign, node, formulaImplementation)
                                        .reduceAlpha(node)
                                },
                            ),
                        )
                    }
            }

    fun createGammaChild(): FolFormula =
        formula
            .boundVariables
            .firstOrNull()!!
            .let { firstBv: Variable ->
                formula
                    .boundVariables
                    .asSequence()
                    .drop(1)
                    .fold(
                        SingletonIdempotentSubstitution(
                            firstBv,
                            formulaImplementation.termImplementation.newFreeVariable(firstBv.symbol),
                        ),
                    ) { subst: IdempotentSubstitution, bv: Variable ->
                        subst.composeIdempotent(
                            theta = SingletonIdempotentSubstitution(
                                key = bv,
                                value = formulaImplementation.termImplementation.newFreeVariable(
                                    bv.symbol,
                                ),
                            ),
                            termImplementation = formulaImplementation.termImplementation,
                        )
                    }
                    .let { substitution: IdempotentSubstitution ->
                        // substitution substitutes the bound variables with new, similarly-named free variables
                        formula
                            .subFormula
                            .apply(substitution, formulaImplementation)

                    }
            }

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
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : GammaFormula<ForSome>, NegativeSignedFormula<ForSome>() {

    override var applications: Int = 0

}

data class PositiveForAllFormula(
    override val formula: ForAll,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : GammaFormula<ForAll>, PositiveSignedFormula<ForAll>() {

    override var applications: Int = 0

}
