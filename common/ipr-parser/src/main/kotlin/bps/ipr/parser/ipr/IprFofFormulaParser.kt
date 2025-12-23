package bps.ipr.parser.ipr

import bps.ipr.formulas.AbstractMultiFolFormula
import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Not
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.VariablesBindingFolFormula
import bps.ipr.parser.FolFormulaParser
import bps.ipr.terms.TermImplementation

open class IprFofFormulaParser(
    final override val formulaImplementation: FolFormulaImplementation = FolFormulaImplementation(),
    override val whitespaceParser: IprWhitespaceParser = IprWhitespaceParser,
    termParserFactory: (TermImplementation) -> IprFofTermParser,
) : FolFormulaParser {

    override val termParser: IprFofTermParser = termParserFactory(formulaImplementation.termImplementation)

    /**
     * Attempts to parse the string as a [FolFormula] and returns a pair containing the parsed formula
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a [FolFormula] and the index where parsing stopped, or null if parsing fails.
     */
    override fun String.parseFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()

    fun String.parseBinaryFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()

    fun String.parseBinaryNonAssoc(): Pair<FolFormula<*>, Int>? = TODO()

    fun String.parseUnitFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()

    fun String.parseUnitaryFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()

    fun String.parseQuantifiedFormulaOrNull(): Pair<VariablesBindingFolFormula<*>, Int>? = TODO()

    fun String.parseAtomicFormulaOrNull(): Pair<Predicate, Int>? = TODO()

    fun String.parseUnaryFormulaOrNull(): Pair<Not, Int>? = TODO()

    fun String.parseBinaryAssocOrNull(): Pair<AbstractMultiFolFormula<*>, Int>? = TODO()

    /**
     * @return the pattern of the first found [Regex] and its first index or `null` if none are present.
     */
    fun String.firstOfOrNull(stopAtRegExp: Collection<Regex>): Pair<String, Int>? {
        return null
    }

}
