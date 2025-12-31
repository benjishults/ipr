package bps.ipr.terms

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class TermInterningTest : FreeSpec() {
    init {
        "test interning works as expected on different languages" - {
            "test FolDagTermImplementation (expect everything interned)" - {
                FolDagTermImplementation().use { implementation ->
                    val var1: Variable = implementation.freeVariableForSymbol("x")
                    val var2: Variable = implementation.freeVariableForSymbol("x")
                    val con1: Constant = implementation.constantForSymbol("a")
                    val con2: Constant = implementation.constantForSymbol("a")
                    val term1: ProperFunction =
                        implementation.properFunction(implementation.functorForSymbol("f", 2), listOf(con1, var1))
                    val term2: ProperFunction =
                        implementation.properFunction(
                            implementation.functorForSymbol("f", 2),
                            listOf(
                                implementation.constantForSymbol("a"),
                                implementation.freeVariableForSymbol("x"),
                            ),
                        )
                    "x === x as variables" {
                        (var1 === var2) shouldBe true
                    }
                    "a === a as constants" {
                        (con1 === con2) shouldBe true
                    }
                    "f(a, x) === f(a, x)" {
                        (term1 === term2) shouldBe true
                    }
                }
            }
            "test FolTermImplementation (expect variables and constants interned)" - {
                FolTermImplementation().use { implementation ->
                    val var1: Variable = implementation.freeVariableForSymbol("x")
                    val var2: Variable = implementation.freeVariableForSymbol("x")
                    val con1: Constant = implementation.constantForSymbol("a")
                    val con2: Constant = implementation.constantForSymbol("a")
                    val term1: ProperFunction =
                        implementation.properFunction(implementation.functorForSymbol("f", 2), listOf(con1, var1))
                    val term2: ProperFunction =
                        implementation.properFunction(
                            implementation.functorForSymbol("f", 2),
                            listOf(
                                implementation.constantForSymbol("a"),
                                implementation.freeVariableForSymbol("x"),
                            ),
                        )
                    "x === x as variables" {
                        (var1 === var2) shouldBe true
                    }
                    "a === a as constants" {
                        (con1 === con2) shouldBe true
                    }
                    "f(a, x) !== f(a, x)" {
                        (term1 === term2) shouldBe false
                    }
                }
            }
            "test TermImplementation (expect nothing interned)" - {
                TermImplementation.use { implementation ->
                    val var1: Variable = implementation.freeVariableForSymbol("x")
                    val var2: Variable = implementation.freeVariableForSymbol("x")
                    val con1: Constant = implementation.constantForSymbol("a")
                    val con2: Constant = implementation.constantForSymbol("a")
                    val term1: ProperFunction =
                        implementation.properFunction(implementation.functorForSymbol("f", 2), listOf(con1, var1))
                    val term2: ProperFunction =
                        implementation.properFunction(
                            implementation.functorForSymbol("f", 2),
                            listOf(
                                implementation.constantForSymbol("a"),
                                implementation.freeVariableForSymbol("x"),
                            ),
                        )
                    "x !== x as variables" {
                        (var1 === var2) shouldBe false
                    }
                    "a !== a as constants" {
                        (con1 === con2) shouldBe false
                    }
                    "f(a, x) !== f(a, x)" {
                        (term1 === term2) shouldBe false
                    }
                }
            }
        }
    }
}
