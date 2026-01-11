package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofResult
import bps.ipr.prover.FolTableauProofIncomplete
import bps.ipr.prover.Prover
import bps.ipr.prover.tableau.closing.SimplePreorderTableauClosingAlgorithm
import bps.ipr.prover.tableau.rule.Rule

/**
 * To make a TableauProver create displayable proofs, add this argument to the constructor:
 * ```kotlin
 *    addNodeToTableauListeners = listOf(DisplayingAddNodeToTableauListener)
 * ```
 */
class TableauProver(
    val unifier: FormulaUnifier,
    val formulaImplementation: FolFormulaImplementation,
    val tableauFactory: () -> BaseTableau,
) : Prover<FolFormula, FolProofResult> {

    constructor(
        unifier: FormulaUnifier,
        formulaImplementation: FolFormulaImplementation,
        initialQLimit: Int = 1,
    ) : this(
        unifier, formulaImplementation,
        tableauFactory = {
            BaseTableau(
                initialQLimit = initialQLimit,
                closingAlgorithm = { formulaUnifier: FormulaUnifier ->
                    with(SimplePreorderTableauClosingAlgorithm) { attemptCloseSimplePreorder(formulaUnifier) }
                },
            )
        },
    )

    override fun prove(formula: FolFormula): FolProofResult =
        require(formula.variablesFreeIn.isEmpty()) { "Quantify all free variables before calling prove.  Formula $formula has free variables ${formula.variablesFreeIn}." }
            .run {
                tableauFactory()
                    .apply { setRootForFormula(formula, formulaImplementation) }
                    .let { tableau: BaseTableau ->
                        var result: FolProofResult? = tableau.attemptClose(unifier)
                        while (result === null) {
                            tableau
                                .applicableRules
                                .dequeueNextRuleOrNull()
                                ?.also { rule: Rule ->
                                    rule.apply()
                                    result = tableau.attemptClose(unifier)
                                }
                                ?: run { result = FolTableauProofIncomplete(tableau) }
                        }
                        result!!
                    }
            }
}
