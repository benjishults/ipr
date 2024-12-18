package bps.ipr.terms

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class LanguageArityTest : FreeSpec() {

    init {
        "test arity stuff works on FolTermImplementation" - {
            FolTermImplementation().use { implementation ->
                "f can be both variable and constant but can't have a non-zero arity, now" {
                    val fAsConstant = implementation.constantOrNull("f")
                    val fAsVariable = implementation.variableOrNull("f")
                    fAsConstant.shouldNotBeNull()
                    fAsVariable.shouldNotBeNull()
                    implementation.properFunctionOrNull("f", listOf(fAsConstant)).shouldBeNull()
                }
            }
        }
        "test arity stuff works on TermImplementation" - {
            TermImplementation.use { implementation ->
                "f can be variable, constant, and function with arguments" {
                    val fAsConstant = implementation.constantOrNull("f")
                    val fAsVariable: Variable? = implementation.variableOrNull("f")
                    fAsConstant.shouldNotBeNull()
                    fAsVariable.shouldNotBeNull()
                    implementation.properFunctionOrNull("f", listOf(fAsConstant))
                        .asClue {
                            it.shouldNotBeNull()
                            it.display() shouldBe "f(f())"
                        }
                    implementation.properFunctionOrNull("f", listOf(fAsConstant, fAsVariable))
                        .asClue {
                            it.shouldNotBeNull()
                            it.display() shouldBe "f(f(), f)"
                        }
                }
            }
        }
    }

}
