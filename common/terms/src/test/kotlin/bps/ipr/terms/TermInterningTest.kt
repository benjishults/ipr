package bps.ipr.terms

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class TermInterningTest : FreeSpec() {
    init {
        "test interning works as expected on different languages" - {
            "test FolDagTermImplementation (expect everything interned)" - {
                FolDagTermImplementation().use { implementation ->
                    val var1: Variable = implementation.freeVariableOrNull("x")!!
                    val var2: Variable = implementation.freeVariableOrNull("x")!!
                    val con1: Constant = implementation.constantOrNull("a")!!
                    val con2: Constant = implementation.constantOrNull("a")!!
                    val term1: ProperFunction =
                        implementation.properFunctionOrNull("f", listOf(con1, var1))!!
                    val term2: ProperFunction =
                        implementation.properFunctionOrNull(
                            "f",
                            listOf(
                                implementation.constantOrNull("a")!!,
                                implementation.freeVariableOrNull("x")!!,
                            ),
                        )!!
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
                    val var1: Variable = implementation.freeVariableOrNull("x")!!
                    val var2: Variable = implementation.freeVariableOrNull("x")!!
                    val con1: Constant = implementation.constantOrNull("a")!!
                    val con2: Constant = implementation.constantOrNull("a")!!
                    val term1: ProperFunction =
                        implementation.properFunctionOrNull("f", listOf(con1, var1))!!
                    val term2: ProperFunction =
                        implementation.properFunctionOrNull(
                            "f",
                            listOf(
                                implementation.constantOrNull("a")!!,
                                implementation.freeVariableOrNull("x")!!,
                            ),
                        )!!
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
                    val var1: Variable = implementation.freeVariableOrNull("x")!!
                    val var2: Variable = implementation.freeVariableOrNull("x")!!
                    val con1: Constant = implementation.constantOrNull("a")!!
                    val con2: Constant = implementation.constantOrNull("a")!!
                    val term1: ProperFunction =
                        implementation.properFunctionOrNull("f", listOf(con1, var1))!!
                    val term2: ProperFunction =
                        implementation.properFunctionOrNull(
                            "f",
                            listOf(
                                implementation.constantOrNull("a")!!,
                                implementation.freeVariableOrNull("x")!!,
                            ),
                        )!!
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
