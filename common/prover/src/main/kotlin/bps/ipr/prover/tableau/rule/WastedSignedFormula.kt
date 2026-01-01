package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.prover.tableau.TableauNode

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
        birthPlace: TableauNode,
        mutableList: MutableList<SignedFormula<*>>?,
    ): MutableList<SignedFormula<*>> =
        mutableList
            ?: mutableListOf()
}

data class NegativeWastedFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : WastedSignedFormula<FolFormula>, NegativeSignedFormula<FolFormula>() {}

data class PositiveWastedFormula(
    override val formula: FolFormula,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : WastedSignedFormula<FolFormula>, PositiveSignedFormula<FolFormula>() {}
