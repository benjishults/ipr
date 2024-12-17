package bps.ipr.terms

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TermLanguageTest : FreeSpec() {
    init {
        "test arity stuff works on FolTermLanguage" {
            val language: TermLanguage = FolDagTermLanguage()
            val fAsConstant = language.constantOrNull("f")
            fAsConstant.shouldNotBeNull()
            language.properFunctionOrNull("f", listOf(fAsConstant)).shouldBeNull()
            val fAsVariable = language.variableOrNull("f")
            fAsVariable.shouldNotBeNull()
        }
        "test interning works as expected on different languages" - {
            "test dag language (expect everything interned)" - {
                val language: TermLanguage = FolDagTermLanguage()
                val var1: Variable = language.variableOrNull("x")!!
                val var2: Variable = language.variableOrNull("x")!!
                val con1: Constant = language.constantOrNull("a")!!
                val con2: Constant = language.constantOrNull("a")!!
                val term1: ProperFunction =
                    language.properFunctionOrNull("f", listOf(con1, var1))!!
                val term2: ProperFunction =
                    language.properFunctionOrNull(
                        "f",
                        listOf(
                            language.constantOrNull("a")!!,
                            language.variableOrNull("x")!!,
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
            "test fol term language (expect variables and constants interned)" - {
                val language: TermLanguage = FolTermLanguage()
                val var1: Variable = language.variableOrNull("x")!!
                val var2: Variable = language.variableOrNull("x")!!
                val con1: Constant = language.constantOrNull("a")!!
                val con2: Constant = language.constantOrNull("a")!!
                val term1: ProperFunction =
                    language.properFunctionOrNull("f", listOf(con1, var1))!!
                val term2: ProperFunction =
                    language.properFunctionOrNull(
                        "f",
                        listOf(
                            language.constantOrNull("a")!!,
                            language.variableOrNull("x")!!,
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
            "test term language (expect nothing interned)" - {
                val language: TermLanguage = object : TermLanguage {
                    override fun variableOrNull(symbol: String): Variable? =
                        FreeVariable(symbol)

                    override fun constantOrNull(symbol: String): Constant? =
                        Constant(symbol)

                    override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
                        ProperFunction(symbol, ArgumentList(arguments))
                }
                val var1: Variable = language.variableOrNull("x")!!
                val var2: Variable = language.variableOrNull("x")!!
                val con1: Constant = language.constantOrNull("a")!!
                val con2: Constant = language.constantOrNull("a")!!
                val term1: ProperFunction =
                    language.properFunctionOrNull("f", listOf(con1, var1))!!
                val term2: ProperFunction =
                    language.properFunctionOrNull(
                        "f",
                        listOf(
                            language.constantOrNull("a")!!,
                            language.variableOrNull("x")!!,
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
