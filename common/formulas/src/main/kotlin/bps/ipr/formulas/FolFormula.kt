package bps.ipr.formulas

import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.terms.Variable

sealed class FolFormula : Formula {

    /**
     * Variables that occur free in this term.
     */
    abstract val variablesFreeIn: Set<Variable>

    override fun toString(): String = display()

//    /**
//     * Variables that are bound by variable-binding within this formula.
//     */
//    val variablesBoundIn: Set<BoundVariable>

    abstract fun apply(substitution: IdempotentSubstitution, formulaImplementation: FolFormulaImplementation): FolFormula

}

