package bps.ipr.terms

import bps.ipr.parser.tptp.TptpFofTermParser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec

class SubstitutionTests : FreeSpec() {

    init {
        with(TptpFofTermParser(FolTermImplementation())) {
            val x = termImplementation.freeVariableOrNull("X")!!
            val y = termImplementation.freeVariableOrNull("Y")!!
            val z = termImplementation.freeVariableOrNull("Z")!!
            val ga = "g(a)".parseTermOrNull()!!.first
            val gx = "g(X)".parseTermOrNull()!!.first
            val gy = "g(Y)".parseTermOrNull()!!.first
            "composeIdempotent does not produce idempotent substitutions" - {
                var sigma = SingletonIdempotentSubstitution(x, ga)
                var theta = SingletonIdempotentSubstitution(z, gx)
                "$sigma$theta -> not idempotent" {
                    shouldThrow<IllegalArgumentException> {
                        sigma.composeIdempotent(theta, termImplementation)
                    }
                }
                sigma = SingletonIdempotentSubstitution(x, gy)
                theta = SingletonIdempotentSubstitution(y, gx)
                "$sigma$theta not idempotent" {
                    shouldThrow<IllegalArgumentException> {
                        sigma.composeIdempotent(theta, termImplementation)
                    }
                }
            }
        }
        "illegal due to ordering of variables {a ↦ b, b ↦ a}"
        "illegal due to application/combination rules {a \u21a6 b, b \u21a6 e}"
        "compose {a \u21a6 c, b \u21a6 d}" {}
        "composition is not commutative but probably is for 'valid' substitutions?" - {
            "compose {x \u21a6 f(y), y \u21a6 z} {x \u21a6 a, y \u21a6 b,z \u21a6 y}" {
                // one direction gives {x \u21a6 f(b),z \u21a6 y}
                // the other direction gives {x \u21a6 a, y \u21a6 b}
            }
        }
        "composition is associative"
        """
            |triangular form (what I'll use) [x1 ↦ t1; x2 ↦ t2; . . . ; xn ↦ tn]
            |represents the composition of the individual pairs.
        """.trimMargin()
        "I will always work with (triangular?  maybe?) idempotent substitutions"
        "idempotend <==> domain and variable-range are disjoint"
        "I will always try to normalize to a specific member of the instantiation quasi-ordering equivalence class by ordering variables"
        """
            |I.e., mgus are unique modulo variable renaming.  Thus, an ordering on variables should give us a canonical mgu.
        """.trimMargin()
    }
}
