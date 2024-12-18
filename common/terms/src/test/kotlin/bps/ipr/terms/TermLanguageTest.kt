package bps.ipr.terms

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TermLanguageTest : FreeSpec() {
    init {
        "test arity stuff works on FolTermLanguage" {
            FolTermImplementation().use { implementation ->
                val fAsConstant = implementation.constantOrNull("f")
                fAsConstant.shouldNotBeNull()
                implementation.properFunctionOrNull("f", listOf(fAsConstant)).shouldBeNull()
                val fAsVariable = implementation.termLanguage.toNormalizedVariableOrNull("f")
                fAsVariable.shouldNotBeNull()
            }
        }
        "test interning works as expected on different languages" - {
            "test dag language (expect everything interned)" - {
                FolDagTermImplementation().use { implementation ->
                    val var1: Variable = implementation.variableOrNull("x")!!
                    val var2: Variable = implementation.variableOrNull("x")!!
                    val con1: Constant = implementation.constantOrNull("a")!!
                    val con2: Constant = implementation.constantOrNull("a")!!
                    val term1: ProperFunction =
                        implementation.properFunctionOrNull("f", listOf(con1, var1))!!
                    val term2: ProperFunction =
                        implementation.properFunctionOrNull(
                            "f",
                            listOf(
                                implementation.constantOrNull("a")!!,
                                implementation.variableOrNull("x")!!,
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
            "test fol term language (expect variables and constants interned)" - {
                FolTermImplementation().use { implementation ->
                    val var1: Variable = implementation.variableOrNull("x")!!
                    val var2: Variable = implementation.variableOrNull("x")!!
                    val con1: Constant = implementation.constantOrNull("a")!!
                    val con2: Constant = implementation.constantOrNull("a")!!
                    val term1: ProperFunction =
                        implementation.properFunctionOrNull("f", listOf(con1, var1))!!
                    val term2: ProperFunction =
                        implementation.properFunctionOrNull(
                            "f",
                            listOf(
                                implementation.constantOrNull("a")!!,
                                implementation.variableOrNull("x")!!,
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
            "test term language (expect nothing interned)" - {
                object : TermImplementation {
                    override val termLanguage: TermLanguage = TermLanguage

                    override fun variableOrNull(symbol: String): Variable? =
                        FreeVariable(symbol)

                    override fun constantOrNull(symbol: String): Constant? =
                        Constant(symbol)

                    override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
                        ProperFunction(symbol, ArgumentList(arguments))
                }.use { implementation ->
                    val var1: Variable = implementation.variableOrNull("x")!!
                    val var2: Variable = implementation.variableOrNull("x")!!
                    val con1: Constant = implementation.constantOrNull("a")!!
                    val con2: Constant = implementation.constantOrNull("a")!!
                    val term1: ProperFunction =
                        implementation.properFunctionOrNull("f", listOf(con1, var1))!!
                    val term2: ProperFunction =
                        implementation.properFunctionOrNull(
                            "f",
                            listOf(
                                implementation.constantOrNull("a")!!,
                                implementation.variableOrNull("x")!!,
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
