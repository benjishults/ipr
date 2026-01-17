package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.parser.ipr.IprWhitespaceParser
import bps.ipr.prover.tableau.TableauProver
import bps.ipr.prover.tableau.closing.CondensingFolBranchCloserImpl
import bps.ipr.prover.tableau.closing.FolBranchCloserImpl
import bps.ipr.substitution.EmptySubstitution
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf

class CondenseTest :
    FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }),
    WhitespaceParser by IprWhitespaceParser {

    data class ProverTest(val formula: FolFormula, val expectedResult: ProofResult)

    init {
        "condense test" - {
            // TODO check that condense has happened
            clear()
            val fileAsString = buildString {
                CondenseTest::class.java.classLoader
                    .getResourceAsStream("condense.ipr")!!
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
                    ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(substitution = EmptySubstitution)))
                }
                .toList()
            formulas
                .forEach { (formula, _) ->
                    "attempt ${formula.display(0)} expecting success" {
                        TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = 2,
                            formulaImplementation = this@CondenseTest.formulaImplementation
                        )
                            .prove(formula)
                            .shouldBeInstanceOf<FolProofSuccess<*>>()
                    }
                }
        }
    }
}
