package bps.ipr.parser

import bps.ipr.terms.Term
import java.util.regex.Pattern

interface Parser {

    val whitespace: Pattern

    /**
     * @return [this.length] if the receiver is all whitespace after [startingAt].
     * Otherwise, the index of the first non-whitespace character after [startingAt].
     */
    fun String.indexOfFirstNonWhitespace(startingAt: Int = 0): Int =
        whitespace.matcher(this)
            .let { matcher ->
                var afterKnownGood = startingAt
                while (afterKnownGood < length && matcher.find(afterKnownGood)) {
                    if (matcher.start() == afterKnownGood) {
                        afterKnownGood = matcher.end()
                    } else {
                        return afterKnownGood
                    }
                }
                afterKnownGood
            }

    /**
     * @return [this.length] if the receiver has no whitespace after [startingAt].
     * Otherwise, the index of the first whitespace character after [startingAt].
     */
    fun String.indexOfFirstWhitespace(startingAt: Int = 0): Int =
        whitespace.matcher(this)
            .let { matcher ->
                if (matcher.find(startingAt)) {
                    matcher.start()
                } else {
                    length
                }
            }
}

interface TermParser : Parser {

    /**
     * Attempts to parse the string as a term and returns a pair containing the parsed term
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a term and the index where parsing stopped, or null if parsing fails.
     */
    fun String.parseTermOrNull(): Pair<Term, Int>?

}


