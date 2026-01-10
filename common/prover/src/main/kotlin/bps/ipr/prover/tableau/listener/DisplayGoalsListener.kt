package bps.ipr.prover.tableau.listener

fun interface DisplayGoalsListener {
    fun displayGoals(builder: StringBuilder, indent: Int)
}
fun interface DisplayGoalsCompactListener {
    fun displayGoalsCompact(builder: StringBuilder, maxChars: Int)
}
