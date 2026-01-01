package bps.ipr.prover.tableau.rule

import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Predicate
import bps.ipr.prover.tableau.TableauNode

sealed interface AtomicSignedFormula : SignedFormula<Predicate> {
    override fun apply() =
        TODO("should never be called due to the way addRule works")
}

data class NegativeAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : AtomicSignedFormula, NegativeSignedFormula<Predicate>()

data class PositiveAtomicFormula(
    override val formula: Predicate,
    override val birthPlace: TableauNode,
    override val formulaImplementation: FolFormulaImplementation,
) : AtomicSignedFormula, PositiveSignedFormula<Predicate>()
