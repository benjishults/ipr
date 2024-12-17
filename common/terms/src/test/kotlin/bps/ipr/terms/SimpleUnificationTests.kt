package bps.ipr.terms

import bps.ipr.parser.tptp.TptpFofTermParser
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SimpleUnificationTests : FreeSpec(), TptpFofTermParser by TptpFofTermParser(FolDagTermLanguage()) {

    init {
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

    fun testUnify(term1: Term, term2: Term, expectedSubstitution: String) {
        "${term1.display()} unify ${term2.display()} expecting $expectedSubstitution" {
            term1.unifyOrNull(term2)
                .asClue {
                    it.shouldNotBeNull()
                    it.display() shouldBe expectedSubstitution
                }
        }
    }

    fun testFailToUnify(term1: Term, term2: Term) {

    }

}
