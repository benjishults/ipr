package bps.ipr.formulas

import bps.ipr.terms.Constant
import bps.ipr.terms.FreeVariable
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PredicateTest : FreeSpec() {

    init {
        with(FolFormulaImplementation()) {
            val P = predicateOrNull("P")!!
            val x: FreeVariable = termImplementation.freeVariableOrNull("x")!!
            val c: Constant = termImplementation.constantOrNull("c")!!
            val Qx = predicateOrNull("Q", listOf(x))!!
            val Rxc = predicateOrNull("R", listOf(x, c))!!
            "plain predicate" {
                P.display() shouldBe "P()"
                predicateOrNull("P", listOf(x)).shouldBeNull()
                Qx.display() shouldBe "Q(x)"
            }
            val impliesOrNull = impliesOrNull(P, Rxc)
            "impliesOrNull" {
                impliesOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display() shouldBe "(P() IMPLIES R(x, c()))"
                }
            }
            val andOrNull = andOrNull(P, Qx, Rxc)
            val orOrNull = orOrNull(P, Qx, Rxc)
            val iffOrNull = iffOrNull(P, Qx, Rxc)
            "and or iff" {
                andOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display() shouldBe "(P() AND Q(x) AND R(x, c()))"
                }
                orOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display() shouldBe "(P() OR Q(x) OR R(x, c()))"
                }
                iffOrNull.asClue {
                    it.shouldNotBeNull()
                    it.display() shouldBe "(P() IFF Q(x) IFF R(x, c()))"
                }
            }
            val notOrNull = notOrNull(Qx)
            "not" {
                notOrNull
                    .asClue {
                        it.shouldNotBeNull()
                        it.display() shouldBe "(NOT Q(x))"
                    }
            }
            "nested formulas" {
                iffOrNull(notOrNull, andOrNull, orOrNull, iffOrNull)
                    .asClue {
                        it.shouldNotBeNull()
                        it.display() shouldBe "((NOT Q(x)) IFF (P() AND Q(x) AND R(x, c())) IFF (P() OR Q(x) OR R(x, c())) IFF (P() IFF Q(x) IFF R(x, c())))"
                    }
            }
            "forAll" {
//                forAllOrNull()
            }

        }
    }

}
