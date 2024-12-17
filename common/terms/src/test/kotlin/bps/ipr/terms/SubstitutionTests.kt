package bps.ipr.terms

import io.kotest.core.spec.style.FreeSpec

class SubstitutionTests : FreeSpec() {

    init {
        "illegal due to ordering of variables {a ↦ b, b ↦ a}"
        "illegal due to application/combination rules {a \u21a6 b, b \u21a6 e}"
        "combine {a \u21a6 c, b \u21a6 d}" {}
    }
}
