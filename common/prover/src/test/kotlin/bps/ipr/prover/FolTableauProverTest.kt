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
import io.kotest.matchers.types.shouldBeInstanceOf

class FolTableauProverTest :
    FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }),
    WhitespaceParser by IprWhitespaceParser {
    data class ProverTest(val formula: FolFormula, val expectedResult: ProofResult)

    init {
        repeat(2) { index ->
            "q-limit = ${index + 1}" - {
                clear()
                val fileAsString = buildString {
                    FolTableauProverTest::class.java.classLoader
                        .getResourceAsStream("fol-q=${index + 1}.ipr")!!
                        .bufferedReader()
                        .useLines { lines: Sequence<String> ->
                            lines.forEach {
                                append(it)
                                append('\n')
                            }
                        }
                }
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
                        "attempt ${formula.display()} expecting success" {
                            TableauProver(GeneralRecursiveDescentFormulaUnifier(), index + 1)
                                .prove(formula, this@FolTableauProverTest.formulaImplementation)
                                .shouldBeInstanceOf<FolProofSuccess>()
                        }
                    }
            }
        }
    }
}
