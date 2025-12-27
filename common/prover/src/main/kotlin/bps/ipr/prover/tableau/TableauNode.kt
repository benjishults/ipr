package bps.ipr.prover.tableau

class TableauNode(
    val tableau: Tableau,
    var parent: TableauNode? = null,
    val newHyps: MutableList<SignedFormula<*>> = mutableListOf(),
    val newGoals: MutableList<SignedFormula<*>> = mutableListOf(),
    var children: MutableList<TableauNode> = mutableListOf(),
)  {
}
