package bps.ipr.terms

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class LanguageArityTest : FreeSpec() {

    init {
        "test arity stuff works on FolTermImplementation" - {
            FolTermImplementation().use { implementation ->
                "f can be both variable and constant but can't have a non-zero arity, now" {
                    val fAsConstant = implementation.constantForSymbol("f")
                    val fAsVariable = implementation.freeVariableForSymbol("f")
                    fAsConstant.shouldNotBeNull()
                    fAsVariable.shouldNotBeNull()
                    shouldThrow<ArityOverloadException> {
                        implementation.functorForSymbol("f", 1)
                    }
                }
            }
        }
        "test arity stuff works on TermImplementation" - {
            TermImplementation.use { implementation ->
                "f can be variable, constant, and function with arguments" {
                    val fAsConstant = implementation.constantForSymbol("f")
                    val fAsVariable: Variable? = implementation.freeVariableForSymbol("f")
                    fAsConstant.shouldNotBeNull()
                    fAsVariable.shouldNotBeNull()
                    implementation.properFunction(implementation.functorForSymbol("f", 1), listOf(fAsConstant))
                        .asClue {
                            it.shouldNotBeNull()
                            it.display() shouldBe "f(f())"
                        }
                    implementation.properFunction(implementation.functorForSymbol("f", 1), listOf(fAsConstant, fAsVariable))
                        .asClue {
                            it.shouldNotBeNull()
                            it.display() shouldBe "f(f(), f)"
                        }
                }
            }
        }
    }

}
