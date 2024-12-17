package bps.ipr.parser.tptp

import bps.ipr.terms.Constant
import bps.ipr.terms.FolDagTermLanguage
import bps.ipr.terms.FreeVariable
import bps.ipr.terms.ProperFunction
import bps.ipr.terms.Term
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class TptpTermParserTest : FreeSpec() {

    init {
        val termLanguage = FolDagTermLanguage()
        with(TptpFofTermParser(termLanguage)) {
            testValidStartTerm<Constant>("f)", "f()", 1)
            testValidStartTerm<ProperFunction>("f(a)c(", "f(a())", 4)
            testValidStartTerm<ProperFunction>("f(a)c,", "f(a())", 4)
            testValidStartTerm<ProperFunction>("f(a)c)", "f(a())", 4)
            testValidStartTerm<ProperFunction>("f(a) c(", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a) c,", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a) c)", "f(a())", 5)
            testValidStartTerm<Constant>(" a b ", "a()", 3)
            testValidStartTerm<Constant>("a, b", "a()", 1)
            testValidStartTerm<ProperFunction>("f(a) c", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a) )c", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a))c", "f(a())", 4)

            testValidTerm<Constant>(" a ", "a()")
            testValidTerm<ProperFunction>("f(A)") {
                arguments.size shouldBe 1
                arguments[0].shouldBeInstanceOf<FreeVariable>()
            }
            testValidTerm<ProperFunction>("f(a)", "f(a())") {
                arguments.size shouldBe 1
                arguments[0].shouldBeInstanceOf<Constant>()
            }
            testValidTerm<ProperFunction>("f(A, b)", "f(A, b())") {
                arguments.size shouldBe 2
                arguments[0].shouldBeInstanceOf<FreeVariable>()
                arguments[1].shouldBeInstanceOf<Constant>()
            }
            testValidTerm<ProperFunction>("f(g(a, b), b)", "f(g(a(), b()), b())") {
                arguments.size shouldBe 2
                with(arguments[0]) {
                    shouldBeInstanceOf<ProperFunction>()
                    arguments.size shouldBe 2
                    arguments[0].shouldBeInstanceOf<Constant>()
                    arguments[1].shouldBeInstanceOf<Constant>()
                }
                arguments[1].shouldBeInstanceOf<Constant>()
            }
            testValidTerm<ProperFunction>(" f ( g ( a , b ) , b ) ", "f(g(a(), b()), b())") {
                arguments.size shouldBe 2
                with(arguments[0]) {
                    shouldBeInstanceOf<ProperFunction>()
                    arguments.size shouldBe 2
                    arguments[0].shouldBeInstanceOf<Constant>()
                    arguments[1].shouldBeInstanceOf<Constant>()
                }
                arguments[1].shouldBeInstanceOf<Constant>()
            }
            "invalid terms" - {
                listOf(
                    "f(a,)",
                    "F(a)",
                    "_",
                    "",
                    " ",
                    "()",
                    "(a)",
                    "f()",
                    "f( )",
                    "f(",
                )
                    .forEach { invalidTermInput ->
                        "test '$invalidTermInput'" {
                            invalidTermInput.parseTermOrNull()
                                .asClue { invalidTerm ->
                                    invalidTerm.shouldBeNull()
                                }
                            termLanguage.clear()
                        }
                    }
            }

        }
    }

    private inline fun <reified T : Term> TptpFofTermParser.testValidTerm(
        tptpStringInput: String,
        expectedDisplay: String = tptpStringInput,
        crossinline details: T.() -> Unit = {},
    ): String {
        "test '$tptpStringInput'" {
            tptpStringInput
                .parseTermOrNull()
                .asClue { pair: Pair<Term, Int>? ->
                    pair.shouldNotBeNull()
                    val (term, indexAfterTerm) = pair
                    indexAfterTerm shouldBe tptpStringInput.length
                    term.display() shouldBe expectedDisplay
                    term.shouldBeInstanceOf<T>()
                    term.details()
                }
            termLanguage.clear()
        }
        return tptpStringInput
    }

    private inline fun <reified T : Term> TptpFofTermParser.testValidStartTerm(
        tptpStringInput: String,
        expectedDisplay: String,
        expectedEndIndex: Int,
        crossinline details: T.() -> Unit = {},
    ): String {
        "test '$tptpStringInput'" {
            tptpStringInput
                .parseTermOrNull()
                .asClue { pair: Pair<Term, Int>? ->
                    pair.shouldNotBeNull()
                    val (term, indexAfterTerm) = pair
                    indexAfterTerm shouldBe expectedEndIndex
                    term.display() shouldBe expectedDisplay
                    term.shouldBeInstanceOf<T>()
                    term.details()
                }
            termLanguage.clear()
        }
        return tptpStringInput
    }

}
