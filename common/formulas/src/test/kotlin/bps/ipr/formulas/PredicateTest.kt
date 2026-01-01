package bps.ipr.formulas

import bps.ipr.terms.Constant
import bps.ipr.terms.FreeVariable
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class PredicateTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val P = predicateOrNull("P")!!
            val x: FreeVariable = termImplementation.freeVariableForSymbol("x")!!
            val c: Constant = termImplementation.constantForSymbol("c")!!
            val Qx = predicateOrNull("Q", listOf(x))!!
            "plain predicate" {
                P.display(0) shouldBe "P()"
                predicateOrNull("P", listOf(x)).shouldBeNull()
                Qx.display(0) shouldBe "Q(x)"
            }
        }
    }

}
