package bps.ipr.prover

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.GeneralRecursiveDescentFormulaUnifier
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.parser.ipr.IprFofFormulaParser
import bps.ipr.parser.ipr.IprFofTermParser
import bps.ipr.parser.ipr.IprWhitespaceParser
import bps.ipr.prover.tableau.AddNodeToTableauListener
import bps.ipr.prover.tableau.DisplayableTableauNodeHelper
import bps.ipr.prover.tableau.TableauProver
import bps.ipr.substitution.Substitution
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ProofPresentationTest :
    FreeSpec(),
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
                            0 -> """---
Suppose
Show
 (FORALL (a b) (IMPLIES (FORALL (z) (IMPLIES (q z) (p z))) (EXISTS (x) (AND (IMPLIES (p x) (p a)) (IMPLIES (q x) (p b))))))
---
 Suppose
  (FORALL (z) (IMPLIES (q z) (p z)))
 Show
  (EXISTS (x) (AND (IMPLIES (p x) (p (a))) (IMPLIES (q x) (p (b)))))
---
  Suppose
  Show
   (AND (IMPLIES (p x_0) (p (a))) (IMPLIES (q x_0) (p (b))))
---
   Suppose
    (p x_0)
   Show
    (p (a))
---
    Suppose
     (IMPLIES (q z_0) (p z_0))
    Show
---
     Suppose
      (p z_0)
     Show
---
      Suppose
      Show
       (AND (IMPLIES (p x_1) (p (a))) (IMPLIES (q x_1) (p (b))))
---
       Suppose
        (p x_1)
       Show
        (p (a))
---
       Suppose
        (q x_1)
       Show
        (p (b))
---
     Suppose
     Show
      (q z_0)
---
      Suppose
      Show
       (AND (IMPLIES (p x_2) (p (a))) (IMPLIES (q x_2) (p (b))))
---
       Suppose
        (p x_2)
       Show
        (p (a))
---
       Suppose
        (q x_2)
       Show
        (p (b))
---
   Suppose
    (q x_0)
   Show
    (p (b))
---
    Suppose
     (IMPLIES (q z_1) (p z_1))
    Show
---
     Suppose
      (p z_1)
     Show
---
      Suppose
      Show
       (AND (IMPLIES (p x_3) (p (a))) (IMPLIES (q x_3) (p (b))))
---
     Suppose
     Show
      (q z_1)
---
      Suppose
      Show
       (AND (IMPLIES (p x_4) (p (a))) (IMPLIES (q x_4) (p (b))))
"""
                            1 -> """---
Suppose
 (IMPLIES (r0) (EXISTS (x) (AND (a x) (b x))))
 (IMPLIES (AND (p0) (q0)) (r0))
 (EXISTS (x) (a x))
 (EXISTS (x) (b x))
Show
 (EXISTS (x) (AND (IMPLIES (p0) (a x)) (IMPLIES (q0) (b x))))
---
 Suppose
  (a (x))
 Show
---
  Suppose
   (b (x_0))
  Show
---
   Suppose
    (EXISTS (x) (AND (a x) (b x)))
   Show
---
    Suppose
     (a (x_1))
     (b (x_1))
    Show
---
     Suppose
      (r0)
     Show
---
      Suppose
      Show
       (AND (IMPLIES (p0) (a x_6)) (IMPLIES (q0) (b x_6)))
---
       Suppose
        (p0)
       Show
        (a x_6)
---
       Suppose
        (q0)
       Show
        (b x_6)
---
     Suppose
     Show
      (AND (p0) (q0))
---
      Suppose
      Show
       (p0)
---
       Suppose
       Show
        (AND (IMPLIES (p0) (a x_9)) (IMPLIES (q0) (b x_9)))
---
        Suppose
         (p0)
        Show
         (a x_9)
---
        Suppose
         (q0)
        Show
         (b x_9)
---
      Suppose
      Show
       (q0)
---
       Suppose
       Show
        (AND (IMPLIES (p0) (a x_10)) (IMPLIES (q0) (b x_10)))
---
        Suppose
         (p0)
        Show
         (a x_10)
---
        Suppose
         (q0)
        Show
         (b x_10)
---
   Suppose
   Show
    (r0)
---
    Suppose
     (r0)
    Show
---
     Suppose
     Show
      (AND (IMPLIES (p0) (a x_5)) (IMPLIES (q0) (b x_5)))
---
      Suppose
       (p0)
      Show
       (a x_5)
---
      Suppose
       (q0)
      Show
       (b x_5)
---
    Suppose
    Show
     (AND (p0) (q0))
---
     Suppose
     Show
      (p0)
---
      Suppose
      Show
       (AND (IMPLIES (p0) (a x_7)) (IMPLIES (q0) (b x_7)))
---
       Suppose
        (p0)
       Show
        (a x_7)
---
       Suppose
        (q0)
       Show
        (b x_7)
---
     Suppose
     Show
      (q0)
---
      Suppose
      Show
       (AND (IMPLIES (p0) (a x_8)) (IMPLIES (q0) (b x_8)))
---
       Suppose
        (p0)
       Show
        (a x_8)
---
       Suppose
        (q0)
       Show
        (b x_8)
"""
                            else -> error("unexpected index")
                        },
                        when (i) {
                            0 -> "{z_0 ↦ (a), x_2 ↦ (a), z_1 ↦ (b), x_0 ↦ (b)}"
                            1 -> "{x_6 ↦ (x_1), x_9 ↦ (x_1), x_10 ↦ (x_1), x_7 ↦ (x_0), x_8 ↦ (x)}"
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
                            formulaImplementation = this@ProofPresentationTest.formulaImplementation,
                            addNodeToTableauListeners = listOf(
                                AddNodeToTableauListener { node ->
                                    DisplayableTableauNodeHelper()
                                        .also {
                                            node.addPopulateListener(it)
                                            node.addDisplayHypsListener(it)
                                            node.addDisplayGoalsListener(it)
                                        }
                                },
                            ),
                        )
                            .prove(formula)
                        with(folProofResult) {
                            shouldBeInstanceOf<FolTableauProofSuccess>()
                            tableau.display() shouldBe expectedResult
                            substitution.display() shouldBe expectedSubstitution
                        }
                    }
                }
        }
    }
}
