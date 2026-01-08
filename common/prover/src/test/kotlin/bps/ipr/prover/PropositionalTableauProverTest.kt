package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.parser.ipr.IprWhitespaceParser
import bps.ipr.prover.tableau.display.DisplayingAddNodeToTableauListener
import bps.ipr.prover.tableau.TableauProver
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf

class PropositionalTableauProverTest :
    FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }),
    WhitespaceParser by IprWhitespaceParser {
    data class ProverTest(val formula: FolFormula)

    init {
        val fileAsString = buildString {
            PropositionalTableauProverTest::class.java.classLoader.getResourceAsStream("propositional.ipr")!!
                .bufferedReader()
                .useLines { lines: Sequence<String> ->
                    lines.forEach {
                        append(it)
                        append('\n')
                    }
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
                    ProverTest(formula)
                }
                .toList()
            formulas
                .forEach { (formula: FolFormula) ->
                    "attempt ${formula.display(0)} expecting success" {
                        TableauProver(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            formulaImplementation = this@PropositionalTableauProverTest.formulaImplementation,
                            addNodeToTableauListeners = listOf(DisplayingAddNodeToTableauListener),
                        )
                            .prove(formula).shouldBeInstanceOf<FolTableauProofSuccess>()
                    }
                }
        }
    }
}
