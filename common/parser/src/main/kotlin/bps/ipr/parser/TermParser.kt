package bps.ipr.parser

import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation

interface TermParser {

    val termImplementation: TermImplementation

    /**
     * Attempts to parse the string as a term and returns a pair containing the parsed term
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     * Leading whitespace results in `null`.
     *
     * @return a pair consisting of a term and the index where parsing stopped, or null if parsing fails.
     */
    fun String.parseTermOrNull(): Pair<Term, Int>?

    /**
     * expects the receiver to be a non-empty list of terms followed by a list terminator (usually a closed paren).
     * @return the [Pair] of
     * 1. the [List] of [Term]s
     * 2. the index within the receiver of the list terminator terminating the list of arguments
     *
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     */
    fun String.parseArgumentsOrNull(): Pair<List<Term>, Int>?

    /**
     * @return the [Pair] of the first found [Char] and its first index or `null` if none are present.
     */
    fun String.firstOfOrNull(stopAt: Collection<Char>): Pair<Char, Int>? {
        forEachIndexed { index: Int, char: Char ->
            if (char in stopAt) {
                return char to index
            }
        }
        return null
    }

    /**
     * @return the substring of the receiver prior to the first element of the receiver that is not in [skip].
     */
    fun String.beforeFirstMiss(skip: Collection<Char>): String =
        buildString {
            forEach {  char: Char ->
                if (char in skip) {
                    append(char)
                } else {
                    return@buildString
                }
            }
        }

}
