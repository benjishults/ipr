package bps.ipr.parser.ipr

import bps.ipr.formulas.And
import bps.ipr.formulas.Iff
import bps.ipr.formulas.Falsity
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.ForAll
import bps.ipr.formulas.ForSome
import bps.ipr.formulas.Formula
import bps.ipr.formulas.Implies
import bps.ipr.formulas.Not
import bps.ipr.formulas.Or
import bps.ipr.formulas.Predicate
import bps.ipr.formulas.Truth
import bps.ipr.terms.FolDagTermImplementation
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.reflect.KClass

class IprFormulaParserTest : FreeSpec() {
    data class TestValidStartFormula(
        val formulaClass: KClass<out Formula>,
        val iprStringInput: String,
        val expectedDisplay: String,
        val expectedEndIndex: Int,
    )

    init {
        val termImplementation = FolDagTermImplementation()
        val formulaImplementation = FolFormulaImplementation()
        with(
            IprFofFormulaParser(
                formulaImplementation = formulaImplementation,
                whitespaceParser = IprWhitespaceParser,
            ) { IprFofTermParser(it) },
        ) {
            "testValidStartFormula" - {
                listOf(
                    // passing
                    TestValidStartFormula(Predicate::class, "(P)", "P()", 3),
                    TestValidStartFormula(Predicate::class, "(Q x)", "Q(x)", 5),
                    TestValidStartFormula(Predicate::class, "(Q (a))", "Q(a())", 7),
                    TestValidStartFormula(Predicate::class, "(R x y)", "R(x, y)", 7),
                    TestValidStartFormula(Predicate::class, "(R x (b))", "R(x, b())", 9),
                    TestValidStartFormula(Predicate::class, "(R (a) (b))", "R(a(), b())", 11),
                    TestValidStartFormula(Predicate::class, "(R (a) y)", "R(a(), y)", 9),
                    TestValidStartFormula(Truth::class, "(truth)", "(TRUTH)", 7),
                    TestValidStartFormula(Falsity::class, "(falsity)", "(FALSITY)", 9),
                    TestValidStartFormula(Not::class, "(not (P))", "(NOT P())", 9),
                    TestValidStartFormula(And::class, "(and (P) (Q x))", "(P() AND Q(x))", 15),
                    TestValidStartFormula(Or::class, "(or (P) (Q x))", "(P() OR Q(x))", 14),
                    TestValidStartFormula(Implies::class, "(implies (P) (Q x))", "(P() IMPLIES Q(x))", 19),
                    TestValidStartFormula(Iff::class, "(iff (P) (Q x))", "(P() IFF Q(x))", 15),
                    TestValidStartFormula(ForSome::class, "(exists ((x)) (Q x))", "(EXISTS (x) Q(x))", 20),
                    TestValidStartFormula(ForAll::class, "(forall ((x)) (Q x))", "(FORALL (x) Q(x))", 20),
                    TestValidStartFormula(ForSome::class, "(exists ((x) (y)) (R x y))", "(EXISTS (x, y) R(x, y))", 26),
                    TestValidStartFormula(ForAll::class, "(forall ((x) (y)) (R x y))", "(FORALL (x, y) R(x, y))", 26),
                )
                    .forEach { (formulaClass, iprStringInput, expectedDisplay, expectedEndIndex) ->
                        "test '$iprStringInput'" {
                            iprStringInput
                                .parseFormulaOrNull(0)
                                .asClue { pair: Pair<Formula, Int>? ->
                                    pair.shouldNotBeNull()
                                    val (formula, indexAfterTerm) = pair
                                    indexAfterTerm shouldBe expectedEndIndex
                                    formula.display() shouldBe expectedDisplay
                                    formula::class shouldBe formulaClass
                                }
                            termImplementation.clear()
                        }

                    }
            }
            "test invalid start formulas" - {
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
                            invalidTermInput.parseFormulaOrNull(0).shouldBeNull()
                        }
                    }
            }


        }
    }
}
