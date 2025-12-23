package bps.ipr.parser.ipr

import bps.ipr.parser.ParserTestFixture
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class IprWhitespaceParserTest : FreeSpec(), IprWhitespaceParser, ParserTestFixture {

    init {
        "all whitespace tests" - {
            listOf(
                "",
                " ",
                "  ",
                " ; asdfa",
                "; asdfa",
                """
            |
            | ; asdf
            | ;;; asdf
            | """
                    .trimMargin(),
                "/* asdfa */",
                " /* asdfa */ ",
                " /* asdfa */ ; asdf",
                """
            | /* asdfa
            |    asdf
            | */
        """
                    .trimMargin(),
                """
            | /* asdfa */
            | /* asdf */
            | """
                    .trimMargin(),
            )
                .forEach { whitespace: String ->
                    "test as whitespace '$whitespace'" {
                        whitespace.indexOfFirstNonWhitespace() shouldBe whitespace.length
                    }
                }
        }

        "test non whitespace" - {
            listOf(
                """ /* asdfa */ aasdf""" to 13,
                """
            | /* asdfa */
            | /* asdf */
            | aasdf"""
                    .trimMargin() to 26,
                """
            | /* asdfa */
            | ; asdf
            | /* asdf */
            | aasdf"""
                    .trimMargin() to 34,
            )
                .forEach { (input, expected) ->
                    "non-whitespace at $expected: '$input'" {
                        input.indexOfFirstNonWhitespace() shouldBe expected
                    }

                }
        }
    }

}
