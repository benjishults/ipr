package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.prover.tableau.BaseTableauNode

sealed interface WastedSignedFormula<F : FolFormula> : SignedFormula<F> {
    /**
     * Does nothing.
     */
    override fun apply() =
        TODO("should never be called due to the way addRule works")

    /**
     * Deletes itself.
     */
    override fun reduceAlpha(
        birthPlace: BaseTableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
        parent: SignedFormula<*>?
    ): MutableList<SignedFormula<*>> =
        mutableList
            ?: mutableListOf()
}

data class NegativeWastedFormula(
    override val formula: FolFormula,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : WastedSignedFormula<FolFormula>, NegativeSignedFormula<FolFormula>()  {
    init {
        splits = computeSplits()
    }
}

data class PositiveWastedFormula(
    override val formula: FolFormula,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : WastedSignedFormula<FolFormula>, PositiveSignedFormula<FolFormula>()  {
    init {
        splits = computeSplits()
    }
}
