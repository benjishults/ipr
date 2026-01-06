package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Predicate
import bps.ipr.prover.tableau.BaseTableauNode

sealed interface AtomicSignedFormula : SignedFormula<Predicate> {
    override fun apply() =
        TODO("should never be called due to the way addRule works")
}

data class NegativeAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : AtomicSignedFormula, NegativeSignedFormula<Predicate>()

data class PositiveAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: BaseTableauNode,
    override val formulaImplementation: FolFormulaImplementation,
    override val parentFormula: SignedFormula<*>?,
) : AtomicSignedFormula, PositiveSignedFormula<Predicate>()
