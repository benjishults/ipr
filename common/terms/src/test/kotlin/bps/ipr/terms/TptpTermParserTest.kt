package bps.ipr.terms

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class TptpTermParserTest : FreeSpec() {

    init {
        "parse terms" - {
            with(TptpFofTermParser) {
                var tptpStringInput = "f(a)"
                "test $tptpStringInput" {
                    tptpStringInput
                        .parseTptpFofTermOrNull()
                        .asClue { term ->
                            term.shouldNotBeNull()
                            term.display() shouldBe tptpStringInput
                            term.shouldBeInstanceOf<ProperFunction>()
                            term.arguments.size shouldBe 1
                            term.arguments[0].shouldBeInstanceOf<Constant>()
                        }
                }
                "invalid terms" - {
                    listOf("f(a,)", "F(a)", "a, b", "_", "", " ", "()", "(a)", "f()", "f( )", "f(", "f)")
                        .forEach { invalidTermInput ->
                            "test '$invalidTermInput'" {
                                invalidTermInput.parseTptpFofTermOrNull()
                                    .asClue { invalidTerm ->
                                        invalidTerm.shouldBeNull()
                                    }
                            }
                        }
                }
                tptpStringInput = "f(A)"
                "test $tptpStringInput" {
                    tptpStringInput
                        .parseTptpFofTermOrNull()
                        .asClue { term ->
                            term.shouldNotBeNull()
                            term.display() shouldBe tptpStringInput
                            term.shouldBeInstanceOf<ProperFunction>()
                            term.arguments.size shouldBe 1
                            term.arguments[0].shouldBeInstanceOf<FreeVariable>()
                        }
                }
                tptpStringInput = "f(A, b)"
                "test $tptpStringInput" {
                    tptpStringInput
                        .parseTptpFofTermOrNull()
                        .asClue { term ->
                            term.shouldNotBeNull()
                            term.display() shouldBe tptpStringInput
                            term.shouldBeInstanceOf<ProperFunction>()
                            term.arguments.size shouldBe 2
                        }
                }
                tptpStringInput = "f(g(a, b), b)"
                "test $tptpStringInput" {
                    tptpStringInput
                        .parseTptpFofTermOrNull()
                        .asClue { term ->
                            term.shouldNotBeNull()
                            term.display() shouldBe tptpStringInput
                            term.shouldBeInstanceOf<ProperFunction>()
                            term.arguments.size shouldBe 2
                        }
                }
                tptpStringInput = " f ( g ( a , b ) , b ) "
                "test $tptpStringInput" {
                    tptpStringInput
                        .parseTptpFofTermOrNull()
                        .asClue { term ->
                            term.shouldNotBeNull()
                            term.display() shouldBe "f(g(a, b), b)"
                            term.shouldBeInstanceOf<ProperFunction>()
                            term.arguments.size shouldBe 2
                        }
                }
            }
        }

    }
}
