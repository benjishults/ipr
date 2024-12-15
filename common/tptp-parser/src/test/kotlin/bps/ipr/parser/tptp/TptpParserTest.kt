package bps.ipr.parser.tptp

import bps.ipr.parser.ParserTestFixture
import io.kotest.core.spec.style.FreeSpec

class TptpParserTest : FreeSpec(), TptpParser, ParserTestFixture {

    init {
        ""
            .shouldBeAllWhitespace()
        " "
            .shouldBeAllWhitespace()
        "  "
            .shouldBeAllWhitespace()
        " % asdfa"
            .shouldBeAllWhitespace()
        "% asdfa"
            .shouldBeAllWhitespace()
        "/* asdfa */"
            .shouldBeAllWhitespace()
        " /* asdfa */ "
            .shouldBeAllWhitespace()
        " /* asdfa */ % asdf"
            .shouldBeAllWhitespace()

        """
            | /* asdfa
            |    asdf
            | */
        """.trimMargin()
            .shouldBeAllWhitespace()
        """
            | /* asdfa */
            | /* asdf */
            | """
            .trimMargin()
            .shouldBeAllWhitespace()
        """
            | /* asdfa */
            | % asdf
            | /* asdf */ % asdf
            | """
            .trimMargin()
            .shouldBeAllWhitespace()

        """ /* asdfa */ aasdf"""
            .firstNonWhitespaceIndex(13)
        """
            | /* asdfa */
            | /* asdf */
            | aasdf"""
            .trimMargin()
            .firstNonWhitespaceIndex(26)
        """
            | /* asdfa */
            | % asdf
            | /* asdf */
            | aasdf"""
            .trimMargin()
            .firstNonWhitespaceIndex(34)
    }

}
