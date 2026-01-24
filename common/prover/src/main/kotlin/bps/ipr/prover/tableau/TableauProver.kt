package bps.ipr.prover.tableau

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.prover.FolProofResult
import bps.ipr.prover.FolTableauProofIncomplete
import bps.ipr.prover.Prover
import bps.ipr.prover.tableau.closing.BranchCloserExtender
import bps.ipr.prover.tableau.closing.BranchClosingSubstitutionExtender
import bps.ipr.prover.tableau.closing.CondensingBranchCloser
import bps.ipr.prover.tableau.closing.FolBranchCloser
import bps.ipr.prover.tableau.closing.SimplePreorderTableauClosingAlgorithm
import bps.ipr.prover.tableau.rule.Rule

class TableauProver<C>(
    val unifier: FormulaUnifier,
    val formulaImplementation: FolFormulaImplementation,
    val tableauFactory: () -> BaseTableau<C>,
    val closingAlgorithm: (Tableau<BaseTableauNode, C>, FormulaUnifier/*, BranchCloserExtender<C>*/) -> FolProofResult<C>?,
) : Prover<FolFormula, FolProofResult<C>> where C : CondensingBranchCloser, C : FolBranchCloser {

    constructor(
        unifier: FormulaUnifier,
        formulaImplementation: FolFormulaImplementation,
        initialQLimit: Int = 1,
    ) : this(
        unifier = unifier,
        formulaImplementation = formulaImplementation,
        closingAlgorithm = { tableau: Tableau<BaseTableauNode, C>, formulaUnifier: FormulaUnifier/*, extender: BranchCloserExtender<C>*/ ->
            with(SimplePreorderTableauClosingAlgorithm) {
                tableau.attemptCloseSimplePreorder(
                    formulaUnifier = formulaUnifier,
                    branchCloserExtender = BranchClosingSubstitutionExtender as BranchCloserExtender<C>,
                )
            }
        },
        tableauFactory = { BaseTableau(initialQLimit) },
    )

    override fun prove(formula: FolFormula): FolProofResult<C> =
        require(formula.variablesFreeIn.isEmpty()) {
            "Quantify all free variables before calling prove.  Formula $formula has free variables ${formula.variablesFreeIn}."
        }
            .run {
                tableauFactory()
                    .apply { setRootForFormula(formula, formulaImplementation) }
                    .let { tableau: BaseTableau<C> ->
                        var result: FolProofResult<C>? = closingAlgorithm(tableau, unifier)
                        while (result === null) {
                            tableau
                                .applicableRules
                                .dequeueNextRuleOrNull()
                                ?.also { rule: Rule ->
                                    rule.apply()
                                    result = closingAlgorithm(tableau, unifier)
                                }
                                ?: run { result = FolTableauProofIncomplete(tableau) }
                        }
                        result!!
                    }
            }
}
