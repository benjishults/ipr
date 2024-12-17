package bps.ipr.parser

import bps.ipr.terms.Term
import bps.ipr.terms.TermLanguage

interface TermParser : Parser {

    val termLanguage: TermLanguage

    /**
     * Attempts to parse the string as a term and returns a pair containing the parsed term
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a term and the index where parsing stopped, or null if parsing fails.
     */
    fun String.parseTermOrNull(): Pair<Term, Int>?

}


