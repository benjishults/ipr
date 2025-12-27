package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.terms.EmptySubstitution
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class TableauProverTest: FreeSpec() , FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }) {
    data class ProverTest(val formula: FolFormula, val expectedResult: ProofResult)
    init {
        listOf(
            ProverTest(
                formula = "(implies (A) (A))".parseFormulaOrNull()!!.first,
                expectedResult = FolProofSuccess(EmptySubstitution)
            ),
            // passed
            ProverTest(
                formula = "(not (falsity))".parseFormulaOrNull()!!.first,
                expectedResult = FolProofSuccess(EmptySubstitution)
            ),
            ProverTest(
                formula = "(truth)".parseFormulaOrNull()!!.first,
                expectedResult = FolProofSuccess(EmptySubstitution)
            ),
        )
            .forEach { (formula, expectedResult) ->
                "attempt $formula expecting $expectedResult" {
                    TableauProver().prove(formula) shouldBe expectedResult
                }
            }
    }
}
