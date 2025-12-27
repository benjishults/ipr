package bps.ipr.parser.tptp

import org.antlr.v4.Tool

fun main() {
    val tool = Tool()
    tool.loadGrammar("TPTP.bnf")
//    Tool.main(emptyArray())
}
