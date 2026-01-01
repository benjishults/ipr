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
            val P = predicateOrNull("P")!!
            val x: FreeVariable = termImplementation.freeVariableForSymbol("x")!!
            val c: Constant = termImplementation.constantForSymbol("c")!!
            val Qx = predicateOrNull("Q", listOf(x))!!
            val Rxc = predicateOrNull("R", listOf(x, c))!!
            val impliesOrNull = impliesOrNull(listOf(P, Rxc))
            "impliesOrNull" {
                impliesOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(P() IMPLIES R(x, c()))"
                }
            }
            val andOrNull = andOrNull(listOf(P, Qx, Rxc))
            val orOrNull = orOrNull(listOf(P, Qx, Rxc))
            val iffOrNull = iffOrNull(listOf(P, Rxc))
            "and or iff" {
                andOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(P() AND Q(x) AND R(x, c()))"
                }
                orOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(P() OR Q(x) OR R(x, c()))"
                }
                iffOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display(0) shouldBe "(P() IFF R(x, c()))"
                }
            }
            val notOrNull = notOrNull(Qx)
            "not" {
                notOrNull
                    .asClue {
                        it.shouldNotBeNull()
                        it.display(0) shouldBe "(NOT Q(x))"
                    }
            }
            "nested formulas" {
                andOrNull(listOf(notOrNull, andOrNull, orOrNull, iffOrNull))
                    .asClue {
                        it.shouldNotBeNull()
                        it.display(0) shouldBe "((NOT Q(x)) AND (P() AND Q(x) AND R(x, c())) AND (P() OR Q(x) OR R(x, c())) AND (P() IFF R(x, c())))"
                    }
            }
        }
    }

}
