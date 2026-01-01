package bps.ipr.parser.ipr

import bps.ipr.terms.Constant
import bps.ipr.terms.FolDagTermImplementation
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
import kotlin.reflect.KClass

class IprTermParserTest : FreeSpec() {
    data class TestValidStartTerm(
        val termClass: KClass<out Term>,
        val iprStringInput: String,
        val expectedDisplay: String,
        val expectedEndIndex: Int,
    )

    init {
        val termImplementation = FolTermImplementation()
//        val termImplementation = FolDagTermImplementation()
        with(IprFofTermParser(termImplementation)) {
            "testValidStartTerm" - {
                listOf(
                    TestValidStartTerm(FreeVariable::class, "x", "x", 1),
                    TestValidStartTerm(Constant::class, "(a)", "(a)", 3),
                    TestValidStartTerm(ProperFunction::class, "(f x)", "(f x)", 5),
                    TestValidStartTerm(ProperFunction::class, "(f (a))", "(f (a))", 7),
                    TestValidStartTerm(ProperFunction::class, "(g x y)", "(g x y)", 7),
                    TestValidStartTerm(ProperFunction::class, "(g (a) y)", "(g (a) y)", 9),
                    TestValidStartTerm(ProperFunction::class, "(g x (b))", "(g x (b))", 9),
                    TestValidStartTerm(ProperFunction::class, "(g (a) (b))", "(g (a) (b))", 11),
                    TestValidStartTerm(ProperFunction::class, "(f x)c)", "(f x)", 5),
                    TestValidStartTerm(ProperFunction::class, "(f (a))c,", "(f (a))", 7),
                    TestValidStartTerm(ProperFunction::class, "(f (a)))c", "(f (a))", 7),
                    TestValidStartTerm(ProperFunction::class, "(f (a)) c(", "(f (a))", 8),
                    TestValidStartTerm(ProperFunction::class, "(f (a)) c(", "(f (a))", 8),
                    TestValidStartTerm(ProperFunction::class, "(f (a)) c,", "(f (a))", 8),
                    TestValidStartTerm(ProperFunction::class, "(f (a)) c)", "(f (a))", 8),
                    TestValidStartTerm(FreeVariable::class, "y)", "y", 1),
                    TestValidStartTerm(FreeVariable::class, "x b", "x", 2),
                    TestValidStartTerm(ProperFunction::class, "(f (a)) c", "(f (a))", 8),
                    TestValidStartTerm(ProperFunction::class, "(f (a)) )c", "(f (a))", 8),
                )
                    .forEach { (termClass, iprStringInput, expectedDisplay, expectedEndIndex) ->
                        "test '$iprStringInput'" {
                            iprStringInput
                                .parseTermOrNull(0)
                                .asClue { pair: Pair<Term, Int>? ->
                                    pair.shouldNotBeNull()
                                    val (term, indexAfterTerm) = pair
                                    term.display() shouldBe expectedDisplay
                                    indexAfterTerm shouldBe expectedEndIndex
                                    term::class shouldBe termClass
//                                    term.shouldBeInstanceOf<termClass>()
//                                term.details()
                                }
                            termImplementation.clear()
                        }

                    }
            }
            "test invalid start terms" - {
                listOf(
                    "(f x",
                    "(f (c)",
                    // passing
                    " (a) b ",
                    "",
                    "(f",
                    "(f ",
                )
                    .forEach { invalidTermInput ->
                        "term is invalid: '$invalidTermInput'" {
                            invalidTermInput.parseTermOrNull(0).shouldBeNull()
                        }
                    }
            }

//            testValidTerm<Constant>(" (a) ", "a()")
//            testValidTerm<ProperFunction>("(f a)") {
//                arguments.size shouldBe 1
//                arguments[0].shouldBeInstanceOf<FreeVariable>()
//            }
//            testValidTerm<ProperFunction>("f(a)", "f(a())") {
//                arguments.size shouldBe 1
//                arguments[0].shouldBeInstanceOf<Constant>()
//            }
//            testValidTerm<ProperFunction>("f(a, b)", "f(a, b())") {
//                arguments.size shouldBe 2
//                arguments[0].shouldBeInstanceOf<FreeVariable>()
//                arguments[1].shouldBeInstanceOf<Constant>()
//            }
//            testValidTerm<ProperFunction>("f(g(a, b), b)", "f(g(a(), b()), b())") {
//                arguments.size shouldBe 2
//                with(arguments[0]) {
//                    shouldBeInstanceOf<ProperFunction>()
//                    arguments.size shouldBe 2
//                    arguments[0].shouldBeInstanceOf<Constant>()
//                    arguments[1].shouldBeInstanceOf<Constant>()
//                }
//                arguments[1].shouldBeInstanceOf<Constant>()
//            }
//            testValidTerm<ProperFunction>(" f ( g ( a , b ) , b ) ", "f(g(a(), b()), b())") {
//                arguments.size shouldBe 2
//                with(arguments[0]) {
//                    shouldBeInstanceOf<ProperFunction>()
//                    arguments.size shouldBe 2
//                    arguments[0].shouldBeInstanceOf<Constant>()
//                    arguments[1].shouldBeInstanceOf<Constant>()
//                }
//                arguments[1].shouldBeInstanceOf<Constant>()
//            }
//            "invalid terms" - {
//                listOf(
//                    "f(a,)",
//                    "F(a)",
//                    "_",
//                    "",
//                    " ",
//                    "()",
//                    "(a)",
//                    "f()",
//                    "f( )",
//                    "f(",
//                )
//                    .forEach { invalidTermInput ->
//                        "test '$invalidTermInput'" {
//                            invalidTermInput.parseTermOrNull()
//                                .asClue { invalidTerm ->
//                                    invalidTerm.shouldBeNull()
//                                }
//                            termImplementation.clear()
//                        }
//                    }
//            }

        }
    }

    private inline fun <reified T : Term> IprFofTermParser.testValidTerm(
        iprStringInput: String,
        expectedDisplay: String = iprStringInput,
        crossinline details: T.() -> Unit = {},
    ): String {
        "test '$iprStringInput'" {
            iprStringInput
                .parseTermOrNull(0)
                .asClue { pair: Pair<Term, Int>? ->
                    pair.shouldNotBeNull()
                    val (term, indexAfterTerm) = pair
                    indexAfterTerm shouldBe iprStringInput.length
                    term.display() shouldBe expectedDisplay
                    term.shouldBeInstanceOf<T>()
                    term.details()
                }
            termImplementation.clear()
        }
        return iprStringInput
    }

    private inline fun <reified T : Term> IprFofTermParser.testValidStartTerm(
        iprStringInput: String,
        expectedDisplay: String,
        expectedEndIndex: Int,
        crossinline details: T.() -> Unit = {},
    ): String {
        "test '$iprStringInput'" {
            iprStringInput
                .parseTermOrNull(0)
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
        return iprStringInput
    }

}
