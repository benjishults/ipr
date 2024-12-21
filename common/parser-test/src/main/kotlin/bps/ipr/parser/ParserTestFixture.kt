package bps.ipr.parser

import io.kotest.core.spec.style.scopes.FreeSpecRootScope
import io.kotest.matchers.shouldBe

interface ParserTestFixture : WhitespaceParser, FreeSpecRootScope {

    fun String.firstNonWhitespaceIndex(expected: Int) {
        "non-whitespace at $expected: '$this'" {
            indexOfFirstNonWhitespace() shouldBe expected
        }
    }

    fun String.shouldBeAllWhitespace() {
        "test as whitespace '$this'" {
            indexOfFirstNonWhitespace() shouldBe length
        }
    }

}
