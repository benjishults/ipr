package bps.ipr.parser

import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Formula
import bps.ipr.formulas.FormulaImplementation

interface FormulaParser {

    val whitespaceParser: WhitespaceParser
    val formulaImplementation: FormulaImplementation

    /**
     * Attempts to parse the string as a term and returns a pair containing the parsed term
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a term and the index where parsing stopped, or null if parsing fails.
     */
    fun String.parseFormulaOrNull(): Pair<Formula, Int>?

}

interface FolFormulaParser : FormulaParser {
    override val formulaImplementation: FolFormulaImplementation
    val termParser: TermParser
}
