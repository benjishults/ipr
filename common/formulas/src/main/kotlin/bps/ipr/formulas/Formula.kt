package bps.ipr.formulas

interface Formula {

    val symbol: String

    fun display(indent: Int = 0): String

}
