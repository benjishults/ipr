package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.prover.tableau.BaseTableauNode

sealed interface ClosingFormula<F : FolFormula> : SignedFormula<F> {
    override fun apply() = Unit
}

data class NegativeClosingFormula(
    override val formula: FolFormula,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : ClosingFormula<FolFormula>, NegativeSignedFormula<FolFormula>() {
    override fun apply() = Unit
    init {
        splits = computeSplits()
    }
}

data class PositiveClosingFormula(
    override val formula: FolFormula,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : ClosingFormula<FolFormula>, PositiveSignedFormula<FolFormula>() {
    override fun apply() = Unit
    init {
        splits = computeSplits()
    }
}
