package bps.ipr.formulas

import bps.ipr.terms.Constant
import bps.ipr.terms.FreeVariable
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class BoundVariableFormulaTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val P = predicateOrNull("P")!!
            val x: FreeVariable = termImplementation.freeVariableOrNull("x")!!
            val y: FreeVariable = termImplementation.freeVariableOrNull("y")!!
//            val bvx: FreeVariable = termImplementation.freeVariableOrNull("x")!!
//            val bvx: BoundVariable = termImplementation.boundVariableOrNull("x")!!
            val c: Constant = termImplementation.constantOrNull("c")!!
            val Qx = predicateOrNull("Q", listOf(x))!!
            val Qbvx = predicateOrNull("Q", listOf(x))!!
//            val Qbvx = predicateOrNull("Q", listOf(bvx))!!
            val rxy = predicateOrNull("R", listOf(x, y))!!
//            val Rbvxy = predicateOrNull("R", listOf(bvx, y))!!
            "variable binding formulas" - {
                "binding a free variable is allowed" {
                    forAllOrNull(listOf(x), Qx)
                        .asClue {
                            it.shouldNotBeNull()
                            it.display() shouldBe "(FORALL (x) Q(x))"
                        }
                }
                "for some x, R(x,y) is allowed" {
                    forSomeOrNull(listOf(x), rxy)
                        .asClue {
                            it.shouldNotBeNull()
                            it.variablesFreeIn shouldContainExactlyInAnyOrder listOf(y)
                        }
                    rxy.variablesFreeIn shouldContainExactlyInAnyOrder listOf(y, x)
                }
            }
        }
    }

}
