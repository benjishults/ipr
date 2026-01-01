package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.prover.tableau.TableauNode

sealed interface ClosingFormula<F : FolFormula> : SignedFormula<F> {
    override fun apply() = Unit
}

data class NegativeClosingFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : ClosingFormula<FolFormula>, NegativeSignedFormula<FolFormula>() {
    override fun apply() = Unit
}

data class PositiveClosingFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : ClosingFormula<FolFormula>, PositiveSignedFormula<FolFormula>() {
    override fun apply() = Unit
}
