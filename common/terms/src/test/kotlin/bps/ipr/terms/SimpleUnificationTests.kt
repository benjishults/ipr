package bps.ipr.terms

import bps.ipr.parser.tptp.TptpFofTermParser
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SimpleUnificationTests : FreeSpec(), TptpFofTermParser by TptpFofTermParser(FolDagTermImplementation()) {

    init {
        "using GeneralRecursiveDescentUnifier" - {
            with(GeneralRecursiveDescentUnifier(FolTermImplementation())) {
                with(FolTermImplementation()) implementation@{
                    with(TptpFofTermParser(this)) {
                        val x = variableOrNull("X")!!
                        val y = variableOrNull("Y")!!
                        val z = variableOrNull("Z")!!
                        val a = constantOrNull("a")!!
                        val ga = "g(a)".parseTermOrNull()!!.first
                        val gx = "g(X)".parseTermOrNull()!!.first
                        "composeIdempotent does not produce idempotent substitutions" {
                            val sigma = SingletonIdempotentSubstitution(x, ga)
                            val theta = SingletonIdempotentSubstitution(z, gx)
                            shouldThrow<IllegalArgumentException> {
                                sigma.composeIdempotent(theta, termImplementation)
                            }
                        }
                        "unify f(X, g(a), g(Z)) and f(g(Y), g(Y), g(g(X)))" {
                            unify(
                                "f(X, g(a), g(Z))".parseTermOrNull()!!.first,
                                "f(g(Y), g(Y), g(g(X)))".parseTermOrNull()!!.first,
                                EmptySubstitution,
                            )
                                .let { substitution ->
                                    substitution.shouldNotBeNull()
                                    substitution.isIdempotent().shouldBeTrue()
                                    substitution.domain shouldContainExactly setOf(x, y, z)
                                    x.apply(substitution, termImplementation) shouldBe properFunctionOrNull(
                                        "g",
                                        listOf(constantOrNull("a")!!),
                                    )
                                    y.apply(substitution, termImplementation) shouldBe constantOrNull("a")
                                    z.apply(substitution, termImplementation) shouldBe properFunctionOrNull(
                                        "g",
                                        listOf(properFunctionOrNull("g", listOf(constantOrNull("a")!!))!!),
                                    )
                                }
                        }
                    }
                }
            }
        }
//        testUnify(
//            "f(a)".parseTermOrNull()!!.first,
//            "X".parseTermOrNull()!!.first,
//            "X \u21a6 a",
//        )
        /*
        h(x1, x2, . . . , xn, f(y0, y0), f(y1, y1), . . . , f(yn−1, yn−1), yn)
        h(f(x0, x0), f(x1, x1), . . . , f(xn−1, xn−1), y1, y2, . . . , yn, xn)
        {x1 7→ f(x0, x0), x2 7→ f(f(x0, x0), f(x0, x0)), . . . ,
y0 7→ x0, y1 7→ f(x0, x0), y2 7→ f(f(x0, x0), f(x0, x0)), . . .}
         */
//        testUnify(
//            // n=3
//            "h(X1, X2, X3, f(Y0, Y0), f(Y1, Y1), f(Y2, Y2), Y3)".parseTermOrNull()!!.first,
//            "h(f(X0, X0), f(X1, X1), f(X2, X2), Y1, Y2, Y3, X3)".parseTermOrNull()!!.first,
//            "{X1 \u21a6 f(X0, X0), " +
//                    "X2 \u21a6 f(f(X0, X0), f(X0, X0)), " +
//                    "X3 \u21a6 f(f(f(X0, X0), f(X0, X0)), f(f(X0, X0), f(X0, X0))), " +
//                    "Y0 \u21a6 X0, " +
//                    "Y1 \u21a6 f(X0, X0), " +
//                    "Y2 \u21a6 f(f(X0, X0), f(X0, X0)), " +
//                    "Y3 \u21a6 f(f(f(X0, X0), f(X0, X0)), f(f(X0, X0), f(X0, X0)))}",
//        )

    }

    fun Unifier.testUnify(term1: Term, term2: Term) {

    }

    fun Unifier.testFailToUnify(term1: Term, term2: Term) {

    }

}
