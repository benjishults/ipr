package bps.ipr.formulas

import bps.ipr.terms.Variable

sealed interface FolFormula<F : FolFormula<F>> : Formula {

    /**
     * Variables that occur free in this term.
     */
    val variablesFreeIn: Set<Variable>

//    /**
//     * Variables that are bound by variable-binding within this formula.
//     */
//    val variablesBoundIn: Set<BoundVariable>

}

