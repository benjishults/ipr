package bps.ipr.parser

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Formula
import bps.ipr.formulas.FormulaImplementation

interface FormulaParser<T: Formula> {

    val whitespaceParser: WhitespaceParser
    val formulaImplementation: FormulaImplementation
    // TODO make a version that works on a Reader

    /**
     * Attempts to parse the string (from [startIndex]) as a formula and returns a pair containing the parsed formula
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a formula and the index where parsing stopped, or null if parsing fails.
     */
    fun String.parseFormulaOrNull(startIndex: Int = 0): Pair<T, Int>?

    fun clear() {
        formulaImplementation.clear()
    }

}

interface FolFormulaParser : FormulaParser<FolFormula> {
    override val formulaImplementation: FolFormulaImplementation
    val termParser: TermParser

    override fun clear() {
        super.clear()
        termParser.clear()
    }
}
