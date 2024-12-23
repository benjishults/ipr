package bps.ipr.formulas

import bps.ipr.terms.BoundVariable
import bps.ipr.terms.Constant
import bps.ipr.terms.FreeVariable
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class BoundVariableFormulaTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val P = predicateOrNull("P")!!
            val x: FreeVariable = termImplementation.freeVariableOrNull("x")!!
            val y: FreeVariable = termImplementation.freeVariableOrNull("")!!
            val bvx: BoundVariable = termImplementation.boundVariableOrNull("x")!!
            val c: Constant = termImplementation.constantOrNull("c")!!
            val Qx = predicateOrNull("Q", listOf(x))!!
            val Qbvx = predicateOrNull("Q", listOf(bvx))!!
            val Rbvxy = predicateOrNull("R", listOf(bvx, y))!!
            "variable binding formulas" - {
                "binding a free variable with the same display is not allowed" {
                    shouldThrow<IllegalArgumentException> {
                        forAllOrNull(listOf(bvx), Qx)
                    }
                }
                "for some x, R(x,y) is allowed if x is of type BoundVariable" {
                    forSomeOrNull(listOf(bvx), Rbvxy)
                        .asClue {
                            it.shouldNotBeNull()
                            it.variablesFreeIn shouldContainExactlyInAnyOrder listOf(y)
                        }
                    Rbvxy.variablesFreeIn shouldContainExactlyInAnyOrder listOf(y, bvx)
                }
            }
        }
    }

}
