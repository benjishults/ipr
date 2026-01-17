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

const val Q_LIMIT_TO_TRY_FOR_INVALID_FORMULAS = 2

class FolTableauProverTest :
    FreeSpec(),
    FolFormulaParser by IprFofFormulaParser(termParserFactory = { IprFofTermParser(it) }),
    WhitespaceParser by IprWhitespaceParser {

    data class ProverTest(val formula: FolFormula, val expectedResult: ProofResult)

    init {
        "invalid with q-limit = $Q_LIMIT_TO_TRY_FOR_INVALID_FORMULAS" - {
            clear()
            val fileAsString = buildString {
                ProofPresentationTest::class.java.classLoader
                    .getResourceAsStream("invalid.ipr")!!
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
                    ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(EmptySubstitution)))
                }
                .toList()
            formulas
                .forEach { (formula, _) ->
                    "attempt ${formula.display(0)} expecting failure" {
                        TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = Q_LIMIT_TO_TRY_FOR_INVALID_FORMULAS,
                            formulaImplementation = this@FolTableauProverTest.formulaImplementation,
                        )
                            .prove(formula)
                            .shouldBeInstanceOf<FolProofIncomplete<*>>()
                    }
                }
        }
        "invalid formulas: invalid-propositional.ipr" - {
            clear()
            val fileAsString = buildString {
                ProofPresentationTest::class.java.classLoader
                    .getResourceAsStream("invalid-propositional.ipr")!!
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
                    ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(EmptySubstitution)))
                }
                .toList()
            formulas
                .forEach { (formula, expectedResult) ->
                    "attempt ${formula.display(0)} expecting failure" {
                        TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = 2,
                            formulaImplementation = this@FolTableauProverTest.formulaImplementation,
                        )
                            .prove(formula)
                            .shouldBeInstanceOf<FolProofIncomplete<CondensingFolBranchCloserImpl>>()
                    }
                }
        }
        repeat(2) { index ->
            "q-limit = ${index + 1}" - {
                clear()
                val fileAsString = buildString {
                    ProofPresentationTest::class.java.classLoader
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
                        ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(EmptySubstitution)))
                    }
                    .toList()
                formulas
                    .forEach { (formula, _) ->
                        "attempt ${formula.display(0)} expecting success" {
                            TableauProver<CondensingFolBranchCloserImpl>(
                                unifier = GeneralRecursiveDescentFormulaUnifier(),
                                initialQLimit = index + 1,
                                formulaImplementation = this@FolTableauProverTest.formulaImplementation,
                            )
                                .prove(formula)
                                .shouldBeInstanceOf<FolProofSuccess<CondensingFolBranchCloserImpl>>()
                        }
                    }
            }
        }
        "more" - {
            clear()
            val fileAsString = buildString {
                ProofPresentationTest::class.java.classLoader
                    .getResourceAsStream("more.ipr")!!
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
                    ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(EmptySubstitution)))
                }
                .toList()
            formulas
                .forEach { (formula, expectedResult) ->
                    "attempt ${formula.display(0)} expecting success" {
                        TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = 2,
                            formulaImplementation = this@FolTableauProverTest.formulaImplementation,
                        )
                            .prove(formula)
                            .shouldBeInstanceOf<FolProofSuccess<CondensingFolBranchCloserImpl>>()
                    }
                }
        }
        "relations" - {
            clear()
            val fileAsString = buildString {
                ProofPresentationTest::class.java.classLoader
                    .getResourceAsStream("relations.ipr")!!
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
                    ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(EmptySubstitution)))
                }
                .toList()
            formulas
                .forEach { (formula, expectedResult) ->
                    "attempt ${formula.display(0)} expecting success" {
                        TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = 2,
                            formulaImplementation = this@FolTableauProverTest.formulaImplementation,
                        )
                            .prove(formula)
                            .shouldBeInstanceOf<FolProofSuccess<*>>()
                    }
                }
        }
        "tests" - {
            clear()
            val fileAsString = buildString {
                ProofPresentationTest::class.java.classLoader
                    .getResourceAsStream("tests.ipr")!!
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
                    ProverTest(formula, FolProofSuccess(FolBranchCloserImpl(EmptySubstitution)))
                }
                .toList()
            formulas
                .forEach { (formula, expectedResult) ->
                    "attempt ${formula.display(0)} expecting success" {
                        TableauProver<CondensingFolBranchCloserImpl>(
                            unifier = GeneralRecursiveDescentFormulaUnifier(),
                            initialQLimit = 2,
                            formulaImplementation = this@FolTableauProverTest.formulaImplementation,
                        )
                            .prove(formula)
                            .shouldBeInstanceOf<FolProofSuccess<*>>()
                    }
                }
        }
    }
}
