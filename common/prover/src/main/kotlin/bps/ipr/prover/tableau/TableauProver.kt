package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofIncomplete
import bps.ipr.prover.FolProofResult
import bps.ipr.prover.Prover
import bps.ipr.prover.tableau.rule.Rule

class TableauProver(
    val unifier: FormulaUnifier,
    val initialQLimit: Int = 1,
) : Prover<FolFormula, FolFormulaImplementation, FolProofResult> {

    override fun prove(formula: FolFormula, formulaImplementation: FolFormulaImplementation): FolProofResult =
        Tableau(formula, formulaImplementation, initialQLimit)
            .let { tableau: Tableau ->
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
