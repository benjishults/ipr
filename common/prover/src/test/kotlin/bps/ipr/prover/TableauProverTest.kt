package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.prover.tableau.TableauProver
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class TableauProverTest : FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }) {
    data class ProverTest(val formula: FolFormula)

    init {
        listOf(
            ProverTest(
                formula = "(implies (A) (A))".parseFormulaOrNull()!!.first,
            ),
            // passing
            ProverTest(
                formula = "(truth)".parseFormulaOrNull()!!.first,
            ),
            ProverTest(
                formula = "(not (falsity))".parseFormulaOrNull()!!.first,
            ),
        )
//            .reversed()
            .forEach { (formula: FolFormula) ->
                "attempt $formula expecting success" {
                    TableauProver(
                        unifier = GeneralRecursiveDescentFormulaUnifier(),
                        formulaImplementation = this@TableauProverTest.formulaImplementation
                    )
                        .prove(formula).shouldBeInstanceOf<FolTableauProofSuccess>()
                }
            }
    }
}
