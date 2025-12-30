package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.parser.ipr.IprWhitespaceParser
import bps.ipr.substitution.EmptySubstitution
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FolTableauProverTest :
    FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }),
    WhitespaceParser by IprWhitespaceParser {
    data class ProverTest(val formula: FolFormula, val expectedResult: ProofResult)

    init {
        val fileAsString = buildString {
            FolTableauProverTest::class.java.classLoader.getResourceAsStream("fol.ipr")!!
                .bufferedReader()
                .useLines { lines: Sequence<String> ->
                    lines.forEach { append(it) }
                }
        }
        "using parsable file" - {
            var startIndex = fileAsString.indexOfFirstNonWhitespace()
            val formulas = generateSequence {
                fileAsString.parseFormulaOrNull(startIndex)
            }
                .map {
                    it.shouldNotBeNull()
                    val (formula, index) = it
                    startIndex = index
                    ProverTest(formula, FolProofSuccess(EmptySubstitution))
                }
                .toList()
            formulas
                .forEach { (formula, expectedResult) ->
                    "attempt ${formula.display()} expecting $expectedResult" {
                        TableauProver(GeneralRecursiveDescentFormulaUnifier())
                            .prove(formula, this@FolTableauProverTest.formulaImplementation) shouldBe expectedResult
                    }
                }
        }
    }
}
