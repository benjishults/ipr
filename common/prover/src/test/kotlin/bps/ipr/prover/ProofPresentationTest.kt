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
import bps.ipr.prover.tableau.closing.CondensingBranchCloser
import bps.ipr.prover.tableau.closing.CondensingFolBranchCloserImpl
import bps.ipr.prover.tableau.closing.SimplePreorderTableauClosingAlgorithm
import bps.ipr.prover.tableau.display.text.ReadableDisplayTableauListener
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
                            // this proof is longer without condensing
                            0 -> """---
(0) Show
 (FORALL (a b) (IMPLIES (FORALL (z) (IMPLIES (q z) (p z))) (EXISTS (x) (AND (IMPLIES (p x) (p a)) (IMPLIES (q x) (p b))))))
---
 (1) Suppose
  (FORALL (z) (IMPLIES (q z) (p z)))
 Show
  (EXISTS (x) (AND (IMPLIES (p x) (p (a))) (IMPLIES (q x) (p (b)))))
---
  (2) Show
   (AND (IMPLIES (p x_0) (p (a))) (IMPLIES (q x_0) (p (b))))
---
   (3) Suppose
    (p x_0)
   Show
    (p (a))
---
    (5) Suppose
     (IMPLIES (q z_0) (p z_0))
---
     (7) Show // FIXME
---
      (11) Show
       (AND (IMPLIES (p x_1) (p (a))) (IMPLIES (q x_1) (p (b))))
---
       (15) Suppose
        (p x_1)
       Show
        (p (a))
---
       (16) Suppose
        (q x_1)
       Show
        (p (b))
---
     (8) Suppose
     Show
      (q z_0)
---
      (12) Show
       (AND (IMPLIES (p x_2) (p (a))) (IMPLIES (q x_2) (p (b))))
---
   (4) Suppose
    (q x_0)
   Show
    (p (b))
---
    (6) Suppose
     (IMPLIES (q z_1) (p z_1))
---
     (9) Show // FIXME
---
      (13) Show
       (AND (IMPLIES (p x_3) (p (a))) (IMPLIES (q x_3) (p (b))))
---
     (10) Suppose // FIXME
     Show
      (q z_1)
---
      (14) Show
       (AND (IMPLIES (p x_4) (p (a))) (IMPLIES (q x_4) (p (b))))
"""
                            // this proof is longer without condense
                            1 -> """---
(0) Suppose
 (IMPLIES (r0) (EXISTS (x) (AND (a x) (b x))))
 (IMPLIES (AND (p0) (q0)) (r0))
 (EXISTS (x) (a x))
 (EXISTS (x) (b x))
Show
 (EXISTS (x) (AND (IMPLIES (p0) (a x)) (IMPLIES (q0) (b x))))
---
 (1) Show // FIXME
---
  (2) Show // FIXME
---
   (3) Suppose
    (EXISTS (x) (AND (a x) (b x)))
---
    (5) Show // FIXME
---
     (8) Show // FIXME
---
      (15) Show
       (AND (IMPLIES (p0) (a x_6)) (IMPLIES (q0) (b x_6)))
---
       (22) Suppose
        (p0)
       Show
        (a x_6)
---
       (23) Suppose
        (q0)
       Show
        (b x_6)
---
     (9) Show
      (AND (p0) (q0))
---
      (12) Suppose // FIXME
      Show
       (p0)
---
       (18) Show
        (AND (IMPLIES (p0) (a x_9)) (IMPLIES (q0) (b x_9)))
---
      (13) Suppose // FIXME
      Show
       (q0)
---
       (19) Show
        (AND (IMPLIES (p0) (a x_10)) (IMPLIES (q0) (b x_10)))
---
   (4) Suppose // FIXME
   Show
    (r0)
---
    (6) Show // FIXME
---
     (14) Show
      (AND (IMPLIES (p0) (a x_5)) (IMPLIES (q0) (b x_5)))
---
      (20) Suppose
       (p0)
      Show
       (a x_5)
---
      (21) Suppose
       (q0)
      Show
       (b x_5)
---
    (7) Show
     (AND (p0) (q0))
---
     (10) Suppose // FIXME
     Show
      (p0)
---
      (16) Show
       (AND (IMPLIES (p0) (a x_7)) (IMPLIES (q0) (b x_7)))
---
       (24) Suppose
        (p0)
       Show
        (a x_7)
---
       (25) Suppose
        (q0)
       Show
        (b x_7)
---
     (11) Suppose // FIXME
     Show
      (q0)
---
      (17) Show
       (AND (IMPLIES (p0) (a x_8)) (IMPLIES (q0) (b x_8)))
---
       (26) Suppose
        (p0)
       Show
        (a x_8)
---
       (27) Suppose
        (q0)
       Show
        (b x_8)
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
                        val folProofResult: FolProofResult<CondensingFolBranchCloserImpl> =
                            TableauProver(
                                unifier = GeneralRecursiveDescentFormulaUnifier(),
                                formulaImplementation = this@ProofPresentationTest.formulaImplementation,
//                                initialQLimit = 2,
                                closingAlgorithm = { tableau, formulaUnifier: FormulaUnifier ->
                                    with(SimplePreorderTableauClosingAlgorithm) {
                                        tableau.attemptCloseSimplePreorder(
                                            formulaUnifier,
                                            branchCloserExtender = BranchClosingSubstitutionExtender,
                                        )
                                    }
                                },
                                tableauFactory = {
                                    BaseTableau<CondensingFolBranchCloserImpl>(
                                        initialQLimit = 2,
                                    )
                                        .apply {
                                            ReadableDisplayTableauListener(
                                                tableau = this,
                                            ).also { tableauListener: ReadableDisplayTableauListener ->
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
                                .also { tableau.display(it, "readable") }
                                .toString() shouldBe expectedResult
                            branchCloser.substitution.display() shouldBe expectedSubstitution
                        }
                    }
                }
        }
    }
}
