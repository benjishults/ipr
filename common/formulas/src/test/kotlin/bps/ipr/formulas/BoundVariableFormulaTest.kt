package bps.ipr.formulas

import bps.ipr.terms.FreeVariable
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class BoundVariableFormulaTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val x: FreeVariable = termImplementation.freeVariableForSymbol("x")
            val y: FreeVariable = termImplementation.freeVariableForSymbol("y")
            val Qx = predicate("Q", listOf(x))
            val rxy = predicate("R", listOf(x, y))
            "variable binding formulas" - {
                "binding a free variable is allowed" {
                    forAllOrNull(listOf(x), Qx)
                        .asClue {
                            it.shouldNotBeNull()
                            it.display(0) shouldBe "(FORALL (x) (Q x))"
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
