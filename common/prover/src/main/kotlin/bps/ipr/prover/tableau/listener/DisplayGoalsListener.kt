package bps.ipr.prover.tableau.listener

fun interface DisplayGoalsListener {
    fun displayGoals(builder: StringBuilder, indent: Int)
}
