package bps.ipr.parser

import java.util.regex.Pattern

interface WhitespaceParser {

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
