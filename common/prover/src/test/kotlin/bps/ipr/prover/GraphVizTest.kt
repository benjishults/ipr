package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.parser.ipr.IprWhitespaceParser
import bps.ipr.prover.tableau.BaseTableau
import bps.ipr.prover.tableau.TableauProver
import bps.ipr.prover.tableau.display.DisplayingAddNodeToTableauListener
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
node [shape=box]
"0" [label="(0) Suppose
Show
(FORALL (a b) (IMPLIES...
"]
"1" [label="(1) Suppose
(FORALL (z) (IMPLIES (...
Show
(EXISTS (x) (AND (IMPL...
"]
"0" -> "1"
"2" [label="(2) Suppose
Show
(AND (IMPLIES (p x_0) ...
"]
"1" -> "2"
"3" [label="(3) Suppose
(p x_0)
Show
(p (a))
"]
"2" -> "3"
"4" [label="(4) Suppose
(q x_0)
Show
(p (b))
"]
"2" -> "4"
"5" [label="(5) Suppose
(IMPLIES (q z_0) (p z_0))
Show
"]
"3" -> "5"
"6" [label="(6) Suppose
(IMPLIES (q z_1) (p z_1))
Show
"]
"4" -> "6"
"7" [label="(7) Suppose
(p z_0)
Show
"]
"5" -> "7"
"8" [label="(8) Suppose
Show
(q z_0)
"]
"5" -> "8"
"9" [label="(9) Suppose
(p z_1)
Show
"]
"6" -> "9"
"10" [label="(10) Suppose
Show
(q z_1)
"]
"6" -> "10"
"11" [label="(11) Suppose
Show
(AND (IMPLIES (p x_1) ...
"]
"7" -> "11"
"12" [label="(12) Suppose
Show
(AND (IMPLIES (p x_2) ...
"]
"8" -> "12"
"13" [label="(13) Suppose
Show
(AND (IMPLIES (p x_3) ...
"]
"9" -> "13"
"14" [label="(14) Suppose
Show
(AND (IMPLIES (p x_4) ...
"]
"10" -> "14"
"15" [label="(15) Suppose
(p x_1)
Show
(p (a))
"]
"11" -> "15"
"16" [label="(16) Suppose
(q x_1)
Show
(p (b))
"]
"11" -> "16"
}
"""
                            // this proof is longer without condense
                            1 -> """digraph G {
node [shape=box]
"0" [label="(0) Suppose
(IMPLIES (r0) (EXISTS ...
(IMPLIES (AND (p0) (q0...
(EXISTS (x) (a x))
(EXISTS (x) (b x))
Show
(EXISTS (x) (AND (IMPL...
"]
"1" [label="(1) Suppose
(a (x))
Show
"]
"0" -> "1"
"2" [label="(2) Suppose
(b (x_0))
Show
"]
"1" -> "2"
"3" [label="(3) Suppose
(EXISTS (x) (AND (a x)...
Show
"]
"2" -> "3"
"4" [label="(4) Suppose
Show
(r0)
"]
"2" -> "4"
"5" [label="(5) Suppose
(a (x_1))
(b (x_1))
Show
"]
"3" -> "5"
"6" [label="(6) Suppose
(r0)
Show
"]
"4" -> "6"
"7" [label="(7) Suppose
Show
(AND (p0) (q0))
"]
"4" -> "7"
"8" [label="(8) Suppose
(r0)
Show
"]
"5" -> "8"
"9" [label="(9) Suppose
Show
(AND (p0) (q0))
"]
"5" -> "9"
"14" [label="(14) Suppose
Show
(AND (IMPLIES (p0) (a ...
"]
"6" -> "14"
"10" [label="(10) Suppose
Show
(p0)
"]
"7" -> "10"
"11" [label="(11) Suppose
Show
(q0)
"]
"7" -> "11"
"15" [label="(15) Suppose
Show
(AND (IMPLIES (p0) (a ...
"]
"8" -> "15"
"12" [label="(12) Suppose
Show
(p0)
"]
"9" -> "12"
"13" [label="(13) Suppose
Show
(q0)
"]
"9" -> "13"
"20" [label="(20) Suppose
(p0)
Show
(a x_5)
"]
"14" -> "20"
"21" [label="(21) Suppose
(q0)
Show
(b x_5)
"]
"14" -> "21"
"16" [label="(16) Suppose
Show
(AND (IMPLIES (p0) (a ...
"]
"10" -> "16"
"17" [label="(17) Suppose
Show
(AND (IMPLIES (p0) (a ...
"]
"11" -> "17"
"22" [label="(22) Suppose
(p0)
Show
(a x_6)
"]
"15" -> "22"
"23" [label="(23) Suppose
(q0)
Show
(b x_6)
"]
"15" -> "23"
"18" [label="(18) Suppose
Show
(AND (IMPLIES (p0) (a ...
"]
"12" -> "18"
"19" [label="(19) Suppose
Show
(AND (IMPLIES (p0) (a ...
"]
"13" -> "19"
"24" [label="(24) Suppose
(p0)
Show
(a x_7)
"]
"16" -> "24"
"25" [label="(25) Suppose
(q0)
Show
(b x_7)
"]
"16" -> "25"
"26" [label="(26) Suppose
(p0)
Show
(a x_8)
"]
"17" -> "26"
"27" [label="(27) Suppose
(q0)
Show
(b x_8)
"]
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
                        val folProofResult = TableauProver(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = 2,
                            formulaImplementation = this@GraphVizTest.formulaImplementation,
                            addNodeToTableauListeners = listOf(DisplayingAddNodeToTableauListener),
                        )
                            .prove(formula)
                        with(folProofResult) {
                            shouldBeInstanceOf<FolTableauProofSuccess>()
                            (tableau as BaseTableau).displayToDot() shouldBe expectedResult
                            substitution.display() shouldBe expectedSubstitution
                        }
                    }
                }
        }
    }
}
