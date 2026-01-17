package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FormulaUnifier
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.parser.ipr.IprWhitespaceParser
import bps.ipr.prover.tableau.BaseTableau
import bps.ipr.prover.tableau.TableauProver
import bps.ipr.prover.tableau.closing.BranchClosingSubstitutionExtender
import bps.ipr.prover.tableau.closing.CondensingFolBranchCloserImpl
import bps.ipr.prover.tableau.closing.SimplePreorderTableauClosingAlgorithm
import bps.ipr.prover.tableau.display.dot.DotDisplayTableauListener
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GraphVizTest : FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }),
    WhitespaceParser by IprWhitespaceParser {

    data class ProverTest(
        val formula: FolFormula,
        val expectedDisplay: String,
        val expectedSubstitution: String,
    )

    init {
        "presentation of completed proofs" - {
            clear()
            val fileAsString = buildString {
                ProofPresentationTest::class.java.classLoader
                    .getResourceAsStream("present-proofs.ipr")!!
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
                .mapIndexed { i, pair ->
                    pair.shouldNotBeNull()
                    val (formula, index) = pair
                    startIndex = index
                    ProverTest(
                        formula,
                        when (i) {
                            // this proof is longer without condense
                            0 -> """digraph G {
layout=dot
root="0"
node [shape=box]
"0" [
label="Show\l(FORALL (a b) (IMPLIES...\l"
]
"1" [
label="Suppose\l(FORALL (z) (IMPLIES (...\lShow\l(EXISTS (x) (AND (IMPL...\l"
]
"0" -> "1"
"2" [
label="Show\l(AND (IMPLIES (p x_0) ...\l"
]
"1" -> "2"
"3" [
label="Suppose\l(p x_0)\lShow\l(p (a))\l"
]
"2" -> "3"
"4" [
label="Suppose\l(q x_0)\lShow\l(p (b))\l"
]
"2" -> "4"
"5" [
label="Suppose\l(IMPLIES (q z_0) (p z_0))\l"
]
"3" -> "5"
"6" [
label="Suppose\l(IMPLIES (q z_1) (p z_1))\l"
]
"4" -> "6"
"7" [
label="Suppose\l(p z_0)\l"
]
"5" -> "7"
"8" [
label="Show\l(q z_0)\l"
]
"5" -> "8"
"9" [
label="Suppose\l(p z_1)\l"
]
"6" -> "9"
"10" [
label="Show\l(q z_1)\l"
]
"6" -> "10"
"11" [
label="Show\l(AND (IMPLIES (p x_1) ...\l"
]
"7" -> "11"
"12" [
label="Show\l(AND (IMPLIES (p x_2) ...\l"
]
"8" -> "12"
"13" [
label="Show\l(AND (IMPLIES (p x_3) ...\l"
]
"9" -> "13"
"14" [
label="Show\l(AND (IMPLIES (p x_4) ...\l"
]
"10" -> "14"
"15" [
label="Suppose\l(p x_1)\lShow\l(p (a))\l"
]
"11" -> "15"
"16" [
label="Suppose\l(q x_1)\lShow\l(p (b))\l"
]
"11" -> "16"
}
"""
                            // this proof is longer without condense
                            1 -> """digraph G {
layout=dot
root="0"
node [shape=box]
"0" [
label="Suppose\l(IMPLIES (r0) (EXISTS ...\l(IMPLIES (AND (p0) (q0...\l(EXISTS (x) (a x))\l(EXISTS (x) (b x))\lShow\l(EXISTS (x) (AND (IMPL...\l"
]
"1" [
label="Suppose\l(a (x))\l"
]
"0" -> "1"
"2" [
label="Suppose\l(b (x_0))\l"
]
"1" -> "2"
"3" [
label="Suppose\l(EXISTS (x) (AND (a x)...\l"
]
"2" -> "3"
"4" [
label="Show\l(r0)\l"
]
"2" -> "4"
"5" [
label="Suppose\l(a (x_1))\l(b (x_1))\l"
]
"3" -> "5"
"6" [
label="Suppose\l(r0)\l"
]
"4" -> "6"
"7" [
label="Show\l(AND (p0) (q0))\l"
]
"4" -> "7"
"8" [
label="Suppose\l(r0)\l"
]
"5" -> "8"
"9" [
label="Show\l(AND (p0) (q0))\l"
]
"5" -> "9"
"14" [
label="Show\l(AND (IMPLIES (p0) (a ...\l"
]
"6" -> "14"
"10" [
label="Show\l(p0)\l"
]
"7" -> "10"
"11" [
label="Show\l(q0)\l"
]
"7" -> "11"
"15" [
label="Show\l(AND (IMPLIES (p0) (a ...\l"
]
"8" -> "15"
"12" [
label="Show\l(p0)\l"
]
"9" -> "12"
"13" [
label="Show\l(q0)\l"
]
"9" -> "13"
"20" [
label="Suppose\l(p0)\lShow\l(a x_5)\l"
]
"14" -> "20"
"21" [
label="Suppose\l(q0)\lShow\l(b x_5)\l"
]
"14" -> "21"
"16" [
label="Show\l(AND (IMPLIES (p0) (a ...\l"
]
"10" -> "16"
"17" [
label="Show\l(AND (IMPLIES (p0) (a ...\l"
]
"11" -> "17"
"22" [
label="Suppose\l(p0)\lShow\l(a x_6)\l"
]
"15" -> "22"
"23" [
label="Suppose\l(q0)\lShow\l(b x_6)\l"
]
"15" -> "23"
"18" [
label="Show\l(AND (IMPLIES (p0) (a ...\l"
]
"12" -> "18"
"19" [
label="Show\l(AND (IMPLIES (p0) (a ...\l"
]
"13" -> "19"
"24" [
label="Suppose\l(p0)\lShow\l(a x_7)\l"
]
"16" -> "24"
"25" [
label="Suppose\l(q0)\lShow\l(b x_7)\l"
]
"16" -> "25"
"26" [
label="Suppose\l(p0)\lShow\l(a x_8)\l"
]
"17" -> "26"
"27" [
label="Suppose\l(q0)\lShow\l(b x_8)\l"
]
"17" -> "27"
}
"""
                            else -> error("unexpected index")
                        },
                        when (i) {
                            // this substitution has two more elements without condense
                            0 -> "{x_1 ↦ (a), x_0 ↦ (b), z_1 ↦ (b)}"
                            1 -> "{x_6 ↦ (x_1), x_7 ↦ (x_0), x_8 ↦ (x)}"
                            else -> error("unexpected index")
                        },
                    )
                }
                .toList()
            formulas
                .forEach { (formula, expectedResult, expectedSubstitution) ->
                    "attempt ${formula.display(0)} expecting success" {
                        val folProofResult: FolProofResult<*> = TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            formulaImplementation = this@GraphVizTest.formulaImplementation,
                            closingAlgorithm = { tableau, formulaUnifier: FormulaUnifier ->
                                with(SimplePreorderTableauClosingAlgorithm) {
                                    tableau.attemptCloseSimplePreorder(
                                        formulaUnifier = formulaUnifier,
                                        branchCloserExtender = BranchClosingSubstitutionExtender,
                                    )
                                }
                            },
                            tableauFactory = {
                                BaseTableau<CondensingFolBranchCloserImpl>(
                                    initialQLimit = 2,
                                )
                                    .apply {
                                        DotDisplayTableauListener(tableau = this)
                                            .also { tableauListener: DotDisplayTableauListener ->
                                                addAddNodeToTableauListener(tableauListener)
                                                addDisplayTableauListener(tableauListener)
                                            }
                                    }
                            },
                        )
                            .prove(formula)
                        with(folProofResult) {
                            shouldBeInstanceOf<FolTableauProofSuccess<*>>()
                            StringBuilder()
                                .also {
                                    tableau.display(it, "dot")
                                }
                                .toString() shouldBe expectedResult
                            branchCloser.substitution.display() shouldBe expectedSubstitution
                        }
                    }
                }
        }
    }
}
