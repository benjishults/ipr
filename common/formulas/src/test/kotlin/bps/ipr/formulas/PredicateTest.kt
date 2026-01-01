package bps.ipr.formulas

import bps.ipr.terms.ArityOverloadException
import bps.ipr.terms.Constant
import bps.ipr.terms.FreeVariable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class PredicateTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val P = predicate("P")
            val x: FreeVariable = termImplementation.freeVariableForSymbol("x")
            val c: Constant = termImplementation.constantForSymbol("c")
            val Qx = predicate("Q", listOf(x))
            "plain predicate" {
                P.display(0) shouldBe "(P)"
                shouldThrow<ArityOverloadException> { predicate("P", listOf(x)) }
                Qx.display(0) shouldBe "(Q x)"
            }
        }
    }

}
