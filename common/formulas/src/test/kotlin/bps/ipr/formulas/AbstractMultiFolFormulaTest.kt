package bps.ipr.formulas

import bps.ipr.terms.Constant
import bps.ipr.terms.FreeVariable
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class AbstractMultiFolFormulaTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val P = predicate("P")
            val x: FreeVariable = termImplementation.freeVariableForSymbol("x")
            val c: Constant = termImplementation.constantForSymbol("c")
            val Qx = predicate("Q", listOf(x))
            val Rxc = predicate("R", listOf(x, c))
            val impliesOrNull = impliesOrNull(listOf(P, Rxc))
            "impliesOrNull" {
                impliesOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(IMPLIES (P) (R x (c)))"
                }
            }
            val andOrNull = andOrNull(listOf(P, Qx, Rxc))
            val orOrNull = orOrNull(listOf(P, Qx, Rxc))
            val iffOrNull = iffOrNull(listOf(P, Rxc))
            "and or iff" {
                andOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(AND (P) (Q x) (R x (c)))"
                }
                orOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(OR (P) (Q x) (R x (c)))"
                }
                iffOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(IFF (P) (R x (c)))"
                }
            }
            val notOrNull = notOrNull(Qx)
            "not" {
                notOrNull
                    .asClue {
                        it.shouldNotBeNull()
                        it.display(0) shouldBe "(NOT (Q x))"
                    }
            }
            "nested formulas" {
                andOrNull(listOf(notOrNull, andOrNull, orOrNull, iffOrNull))
                    .asClue {
                        it.shouldNotBeNull()
                        it.display(0) shouldBe "(AND (NOT (Q x)) (AND (P) (Q x) (R x (c))) (OR (P) (Q x) (R x (c))) (IFF (P) (R x (c))))"
                    }
            }
        }
    }

}
