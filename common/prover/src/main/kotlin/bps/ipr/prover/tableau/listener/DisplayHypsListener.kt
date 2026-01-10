package bps.ipr.prover.tableau.listener

fun interface DisplayHypsListener {
    fun displayHyps(builder: StringBuilder, indent: Int)
}

fun interface DisplayHypsCompactListener {
    fun displayHypsCompact(builder: StringBuilder, maxChars: Int)
}
