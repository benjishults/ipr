package bps.ipr.formulas

import bps.ipr.terms.Variable

class Not(val subFormula: FolFormula) : FolFormula {
    //    override val variablesBoundIn: Set<BoundVariable> = subFormula.variablesBoundIn
    override val variablesFreeIn: Set<Variable> = subFormula.variablesFreeIn

    override val symbol: String = "NOT"

    override fun display(): String =
        "(NOT ${subFormula.display()})"

}
