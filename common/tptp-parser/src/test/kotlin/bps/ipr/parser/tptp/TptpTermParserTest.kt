package bps.ipr.parser.tptp

import bps.ipr.terms.Constant
import bps.ipr.terms.FolTermImplementation
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
//        val termImplementation = FolDagTermImplementation()
        val termImplementation = FolTermImplementation()
        with(TptpFofTermParser(termImplementation)) {
            testValidStartTerm<Constant>("f)", "f()", 1)
            testValidStartTerm<ProperFunction>("f(a)c(", "f(a())", 4)
            testValidStartTerm<ProperFunction>("f(a)c,", "f(a())", 4)
            testValidStartTerm<ProperFunction>("f(a)c)", "f(a())", 4)
            testValidStartTerm<ProperFunction>("f(a) c(", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a) c,", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a) c)", "f(a())", 5)
            // FIXME let's make this fail?
            testValidStartTerm<Constant>(" a b ", "a()", 3)
            testValidStartTerm<Constant>("a, b", "a()", 1)
            testValidStartTerm<ProperFunction>("f(a) c", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a) )c", "f(a())", 5)
            testValidStartTerm<ProperFunction>("f(a))c", "f(a())", 4)

            // FIXME let's make this fail?
            testValidTerm<Constant>(" a ", "a()")
            testValidTerm<ProperFunction>("f(A)") {
                arguments.count() shouldBe 1
                arguments.elementAt(0).shouldBeInstanceOf<FreeVariable>()
            }
            testValidTerm<ProperFunction>("f(a)", "f(a())") {
                arguments.count() shouldBe 1
                arguments.elementAt(0).shouldBeInstanceOf<Constant>()
            }
            testValidTerm<ProperFunction>("f(A, b)", "f(A, b())") {
                arguments.count() shouldBe 2
                arguments.elementAt(0).shouldBeInstanceOf<FreeVariable>()
                arguments.elementAt(1).shouldBeInstanceOf<Constant>()
            }
            testValidTerm<ProperFunction>("f(g(a, b), b)", "f(g(a(), b()), b())") {
                arguments.count() shouldBe 2
                with(arguments.elementAt(0)) {
                    shouldBeInstanceOf<ProperFunction>()
                    arguments.count() shouldBe 2
                    arguments.elementAt(0).shouldBeInstanceOf<Constant>()
                    arguments.elementAt(1).shouldBeInstanceOf<Constant>()
                }
                arguments.elementAt(1).shouldBeInstanceOf<Constant>()
            }
            // FIXME let's make this fail?
            testValidTerm<ProperFunction>(" f ( g ( a , b ) , b ) ", "f(g(a(), b()), b())") {
                arguments.count() shouldBe 2
                with(arguments.elementAt(0)) {
                    shouldBeInstanceOf<ProperFunction>()
                    arguments.count() shouldBe 2
                    arguments.elementAt(0).shouldBeInstanceOf<Constant>()
                    arguments.elementAt(1).shouldBeInstanceOf<Constant>()
                }
                arguments.elementAt(1).shouldBeInstanceOf<Constant>()
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
                            termImplementation.clear()
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
            termImplementation.clear()
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
            termImplementation.clear()
        }
        return tptpStringInput
    }

}
