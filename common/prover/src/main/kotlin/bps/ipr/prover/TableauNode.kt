package bps.ipr.prover

import bps.ipr.formulas.FolFormula

class TableauNode(
    val parent: TableauNode? = null,
    val newHyps: List<SignedFormula> = emptyList(),
    val newGoals: List<SignedFormula> = emptyList(),
    var children: List<TableauNode> = emptyList(),
)  {
//    companion object {
//        operator fun invoke(formula: FolFormula<*>) = TableauNode(null, newHyps, newGoals)
//    }
}
