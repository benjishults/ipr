package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofIncomplete
import bps.ipr.prover.FolProofResult
import bps.ipr.prover.Prover
import bps.ipr.prover.tableau.rule.Rule

fun interface AddNodeToTableauListener {
    fun addNodeToTableau(node: BaseTableauNode)
}

/**
 * To make a TableauProver create displayable proofs, add this argument to the constructor:
 * ```kotlin
 *                             addNodeToTableauListeners = listOf(
 *                                 AddNodeToTableauListener { node ->
 *                                     DisplayableTableauNodeHelper()
 *                                         .also {
 *                                             node.addPopulateListener(it)
 *                                             node.addDisplayHypsListener(it)
 *                                             node.addDisplayGoalsListener(it)
 *                                         }
 *                                 },
 *                             ),
 * ```
 */

class TableauProver(
    val unifier: FormulaUnifier,
    val initialQLimit: Int = 1,
    val formulaImplementation: FolFormulaImplementation,
    val addNodeToTableauListeners: List<AddNodeToTableauListener>? = null,
//    val ruleAddedListeners: List<RuleAddedListener> = emptyList(),
//    val ruleDequeueListeners: List<RuleDequeueListener> = emptyList(),
) : Prover<FolFormula, FolProofResult> {



    override fun prove(formula: FolFormula): FolProofResult =
        require(formula.variablesFreeIn.isEmpty()) { "Quantify all free variables before calling prove.  Formula $formula has free variables ${formula.variablesFreeIn}." }
            .run {
                BaseTableau(
                    formula = formula,
                    formulaImplementation = formulaImplementation,
                    initialQLimit = initialQLimit,
                    addNodeToTableauListeners = addNodeToTableauListeners,
//            ruleAddedListeners = ruleAddedListeners,
//            ruleDequeueListeners = ruleDequeueListeners
                )
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
                                ?: run { result = FolProofIncomplete }
                        }
                        result!!
                    }
            }
}
