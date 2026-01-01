package bps.ipr.terms

import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.GeneralRecursiveDescentTermUnifier
import bps.ipr.substitution.Substitution
import bps.ipr.substitution.TermUnifier
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SimpleUnificationTests : FreeSpec(), IprFofTermParser by IprFofTermParser(FolTermImplementation()) {
//class SimpleUnificationTests : FreeSpec(), IprFofTermParser by IprFofTermParser(FolDagTermImplementation()) {

    data class TermUnificationTest(
        val term1: Term,
        val term2: Term,
        val termUnifier: TermUnifier,
        val substitutionValidation: (Substitution) -> Unit,
    )

    init {
        "using GeneralRecursiveDescentUnifier with a FolTermImplementation" - {
            val x = termImplementation.freeVariableForSymbol("X")!!
            val y = termImplementation.freeVariableForSymbol("Y")!!
            val z = termImplementation.freeVariableForSymbol("Z")!!
            val ga = "(g (a))".parseTermOrNull()!!.first
            val gx = "(g X)".parseTermOrNull()!!.first
            val a = "(a)".parseTermOrNull()!!.first
            val x1 = "x1".parseTermOrNull()!!.first
            val x0 = "x0".parseTermOrNull()!!.first
            val y1 = "y1".parseTermOrNull()!!.first
            val y0 = "y0".parseTermOrNull()!!.first

            listOf(
                TermUnificationTest(
                    "(f X (g (a)) (g Z))".parseTermOrNull(0)!!.first,
                    "(f (g Y) (g Y) (g (g X)))".parseTermOrNull(0)!!.first,
                    GeneralRecursiveDescentTermUnifier(termImplementation),
                ) { substitution ->
                    substitution.shouldNotBeNull()
                    substitution.isIdempotent().shouldBeTrue()
                    substitution.display() shouldBe "{X ↦ g(a()), Y ↦ a(), Z ↦ g(g(a()))}"
                    substitution.domain shouldContainExactly setOf(x, y, z)
                    x.apply(
                        substitution,
                        termImplementation,
                    ) shouldBe ga
                    y.apply(
                        substitution,
                        termImplementation,
                    ) shouldBe a
                    z.apply(
                        substitution,
                        termImplementation,
                    ) shouldBe "(g (g (a)))".parseTermOrNull()!!.first
                },
                TermUnificationTest(
                    ga,
                    gx,
                    GeneralRecursiveDescentTermUnifier(termImplementation),
                ) { substitution ->
                    substitution.shouldNotBeNull()
                    x.apply(substitution, termImplementation) shouldBe a
                    substitution.withoutBindingsFor(setOf(x)) shouldBe EmptySubstitution
                },
                TermUnificationTest(
                    // n = 1
                    "(h3 x1 (f2 y0 y0) y1)".parseTermOrNull()!!.first,
                    "(h3 (f2 x0 x0) y1 x1)".parseTermOrNull()!!.first,
                    GeneralRecursiveDescentTermUnifier(termImplementation),
                ) { substitution ->
                    substitution.shouldNotBeNull()
                    substitution.toString() shouldBe "{" +
                            "x1 ↦ f2(x0, x0), " +
                            "y1 ↦ f2(x0, x0), " +
                            "y0 ↦ x0}"
                },
                TermUnificationTest(
                    // n = 2
                    "(h5 x1 x2 (f2 y0 y0) (f2 y1 y1) y2)".parseTermOrNull()!!.first,
                    "(h5 (f2 x0 x0) (f2 x1 x1) y1 y2 x2)".parseTermOrNull()!!.first,
                    GeneralRecursiveDescentTermUnifier(termImplementation),
                ) { substitution ->
                    substitution.shouldNotBeNull()
                    substitution.toString() shouldBe "{" +
                            "x1 ↦ f2(x0, x0), " +
                            "x2 ↦ f2(f2(x0, x0), f2(x0, x0)), " +
                            "y1 ↦ f2(x0, x0), " +
                            "y2 ↦ f2(f2(x0, x0), f2(x0, x0)), " +
                            "y0 ↦ x0}"
                },
                TermUnificationTest(
                    // n = 3
                    "(h7 x1 x2 x3 (f2 y0 y0) (f2 y1 y1) (f2 y2 y2) y3)".parseTermOrNull()!!.first,
                    "(h7 (f2 x0 x0) (f2 x1 x1) (f2 x2 x2) y1 y2 y3  x3)".parseTermOrNull()!!.first,
                    GeneralRecursiveDescentTermUnifier(termImplementation),
                ) { substitution ->
                    substitution.shouldNotBeNull()
                    substitution.toString() shouldBe "{" +
                            "x1 ↦ f2(x0, x0), " +
                            "x2 ↦ f2(f2(x0, x0), f2(x0, x0)), " +
                            "x3 ↦ f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0))), " +
                            "y1 ↦ f2(x0, x0), " +
                            "y2 ↦ f2(f2(x0, x0), f2(x0, x0)), " +
                            "y3 ↦ f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0))), " +
                            "y0 ↦ x0}"
                },
                TermUnificationTest(
                    // n = 4
                    "(h9 x1 x2 x3 x4 (f2 y0 y0) (f2 y1 y1) (f2 y2 y2) (f2 y3 y3) y4)".parseTermOrNull()!!.first,
                    "(h9 (f2 x0 x0) (f2 x1 x1) (f2 x2 x2) (f2 x3 x3) y1 y2 y3 y4 x4)".parseTermOrNull()!!.first,
                    GeneralRecursiveDescentTermUnifier(termImplementation),
                ) { substitution ->
                    substitution.shouldNotBeNull()
                    substitution.toString() shouldBe "{" +
                            "x1 ↦ f2(x0, x0), " +
                            "x2 ↦ f2(f2(x0, x0), f2(x0, x0)), " +
                            "x3 ↦ f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0))), " +
                            "x4 ↦ f2(f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0))), f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0)))), " +
                            "y1 ↦ f2(x0, x0), " +
                            "y2 ↦ f2(f2(x0, x0), f2(x0, x0)), " +
                            "y3 ↦ f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0))), " +
                            "y4 ↦ f2(f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0))), f2(f2(f2(x0, x0), f2(x0, x0)), f2(f2(x0, x0), f2(x0, x0)))), " +
                            "y0 ↦ x0}"
                },
            )
                .forEach { (term1, term2, termUnifier, substitutionValidation) ->
                    "unify $term1 and $term2" {
                        substitutionValidation(termUnifier.unify(term1, term2, EmptySubstitution)!!)
                    }
                }
        }

    }

}
