package bps.ipr.parser.ipr

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.formulas.Formula
import bps.ipr.formulas.Predicate
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.WhitespaceParser
import bps.ipr.terms.FreeVariable
import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation
import bps.ipr.terms.Variable

open class IprFofFormulaParser(
    final override val formulaImplementation: FolFormulaImplementation = FolFormulaImplementation(),
    override val whitespaceParser: IprWhitespaceParser = IprWhitespaceParser,
    termParserFactory: (TermImplementation) -> IprFofTermParser,
) : FolFormulaParser, WhitespaceParser by whitespaceParser {

    final override val termParser: IprFofTermParser = termParserFactory(formulaImplementation.termImplementation)

    override fun String.parseFormulaOrNull(startIndex: Int): Pair<FolFormula<*>, Int>? =
        takeIf { length > startIndex }
            ?.let { get(startIndex) }
            ?.takeIf { it == '(' }
            ?.let {
                with(termParser) {
                    indexOfFirstNonWhitespace(1 + startIndex)
                        .let { indexOfFirstNonWhitespace: Int ->
                            parseAtomOrNull(indexOfFirstNonWhitespace)
                                ?.let { (formulaBuilder: String, indexAfterAtom: Int) ->
                                    when (formulaBuilder) {
                                        "truth" ->
                                            parseSpecial(indexAfterAtom) {
                                                formulaImplementation.truthOrNull()
                                            }
                                        "falsity" ->
                                            parseSpecial(indexAfterAtom) {
                                                formulaImplementation.falsityOrNull()
                                            }
                                        "not" ->
                                            parseLogicalOperator(
                                                startIndex = indexAfterAtom,
                                                sizeConstraint = { it == 1 },
                                            ) {
                                                formulaImplementation.notOrNull(it[0])
                                            }
                                        "and" ->
                                            parseLogicalOperator(
                                                startIndex = indexAfterAtom,
                                                sizeConstraint = { it > 1 },
                                            ) {
                                                formulaImplementation.andOrNull(it)
                                            }
                                        "or" ->
                                            parseLogicalOperator(
                                                startIndex = indexAfterAtom,
                                                sizeConstraint = { it > 1 },
                                            ) {
                                                formulaImplementation.orOrNull(it)
                                            }
                                        "iff" ->
                                            parseLogicalOperator(
                                                startIndex = indexAfterAtom,
                                                sizeConstraint = { it > 1 },
                                            ) {
                                                formulaImplementation.iffOrNull(it)
                                            }
                                        "implies" ->
                                            parseLogicalOperator(
                                                startIndex = indexAfterAtom,
                                                sizeConstraint = { it == 2 },
                                            ) {
                                                formulaImplementation.impliesOrNull(it)
                                            }
                                        "forall" ->
                                            parseBindingFormula(indexAfterAtom) { boundVars, formula ->
                                                formulaImplementation.forAllOrNull(
                                                    boundVars,
                                                    formula,
                                                )
                                            }
                                        "exists" ->
                                            parseBindingFormula(indexAfterAtom) { boundVars, formula ->
                                                formulaImplementation.forSomeOrNull(
                                                    boundVars,
                                                    formula,
                                                )
                                            }
                                        else -> {
                                            parsePredicate(
                                                indexAfterAtom,
                                                formulaBuilder,
                                            )
                                        }
                                    }
                                }
                        }
                }
            }

    private fun String.parseBindingFormula(
        startIndex: Int,
        formulaFactory: (List<Variable>, FolFormula<*>) -> FolFormula<*>,
    ): Pair<FolFormula<*>, Int>? =
        if (startIndex == length)
            null
        else {
            if (get(startIndex) == '(') {
                val index = indexOfFirstNonWhitespace(startIndex + 1)
                parseBoundVarList(index)
                    ?.let { (boundVars: List<FreeVariable>, indexOfFormula: Int) ->
                        parseFormulaArgumentsOrNull(indexOfFormula)
                            ?.let { (formulas: List<FolFormula<*>>, index: Int) ->
                                if (formulas.size == 1)
                                    formulaFactory(
                                        boundVars,
                                        formulas[0],
                                    ) to indexOfFirstNonWhitespace(index + 1)
                                else
                                    null
                            }
                    }
            } else
                null
        }

    /**
     * @return a [Pair] of the [List] of bound [FreeVariable]s and the index within the receiver of the first
     * non-whitespace after the closing paren that ends the list.
     */
    private fun String.parseBoundVarList(startIndex: Int): Pair<List<FreeVariable>, Int>? =
        with(termParser) {
            val boundVars = mutableListOf<FreeVariable>()
            var runningStartingIndex = startIndex
            while (true) {
                if (runningStartingIndex < length) {
                    when (get(runningStartingIndex)) {
                        '(' -> {
                            val indexOfBoundVar = indexOfFirstNonWhitespace(runningStartingIndex + 1)
                            parseAtomOrNull(indexOfBoundVar)
                                ?.let { (boundVar: String, localIndexOfCloseParen: Int) ->
                                    if (get(localIndexOfCloseParen) == ')') {
                                        boundVars.add(termImplementation.freeVariableOrNull(boundVar)!!)
                                        runningStartingIndex = indexOfFirstNonWhitespace(localIndexOfCloseParen + 1)
                                    } else
                                        return null
                                }
                                ?: return null
                        }
                        ')' -> return boundVars to indexOfFirstNonWhitespace(runningStartingIndex + 1)
                        else -> return null
                    }
                } else
                    return null
            }
            // NOTE not sure why Kotlin requires me to put this here then warns me that it's unreachable.
            return null
        }


    private fun String.parseLogicalOperator(
        startIndex: Int,
        sizeConstraint: (Int) -> Boolean = { true },
        formulaFactory: (List<FolFormula<*>>) -> FolFormula<*>,
    ): Pair<FolFormula<*>, Int>? =
        parseFormulaArgumentsOrNull(startIndex)
            ?.let { (args: List<FolFormula<*>>, indexOfEndingParen: Int) ->
                if (sizeConstraint(args.size)) {
                    formulaFactory(args) to indexOfFirstNonWhitespace(indexOfEndingParen + 1)
                } else {
                    null
                }
            }

    /**
     * expects the receiver to be a non-empty, comma-separated list of FOF formulas (with no initial open paren)
     * followed by a closed paren then, possibly, other stuff.
     * @return the [Pair] of
     * 1. the [List] of [FolFormula]s
     * 2. the index within the receiver of the `')'` that terminates the list of formulas
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     * @throws ArityOverloadException if a symbol is used with an arity that differs from the arity of that symbol
     * in the language
     */
    fun String.parseFormulaArgumentsOrNull(startIndex: Int): Pair<List<FolFormula<*>>, Int>? =
        parseIprFofFormulaArgumentsOrNullHelper(startIndex)

    private fun String.parseIprFofFormulaArgumentsOrNullHelper(
        startIndex: Int,
        list: MutableList<FolFormula<*>> = mutableListOf(),
    ): Pair<List<FolFormula<*>>, Int>? =
        parseFormulaOrNull(startIndex)
            ?.let { (nextFormula: FolFormula<*>, indexAfterFirstFormula: Int) ->
                list.add(nextFormula)
                getOrNull(indexAfterFirstFormula)
                    ?.let { delimiter: Char ->
                        // NOTE need to say return here to prevent the ?: below from activating on a null value
                        return when (delimiter) {
                            ')' ->
                                list to indexAfterFirstFormula
                            else ->
                                parseIprFofFormulaArgumentsOrNullHelper(indexAfterFirstFormula, list)
                        }
                    }
            }
            ?: takeIf { length > startIndex && get(startIndex) == ')' }
                ?.let { emptyList<FolFormula<*>>() to startIndex }


    private fun String.parsePredicate(
        startIndex: Int,
        formulaBuilder: String,
    ): Pair<Predicate, Int>? =
        with(termParser) {
            parseArgumentsOrNull(startIndex)
                ?.let { (args: List<Term>, indexOfClosedParen: Int) ->
                    val globalIndexAfterWhitespaceAfterTerm = indexOfFirstNonWhitespace(indexOfClosedParen + 1)
                    takeIf { args.isEmpty() }
                        ?.let {
                            (formulaImplementation.predicateOrNull(formulaBuilder)
                                ?: throw ArityOverloadException(
                                    "$formulaBuilder already has arity ${
                                        formulaImplementation.formulaLanguage.getPredicateArity(formulaBuilder)
                                    }",
                                )) to globalIndexAfterWhitespaceAfterTerm
                        }
                        ?: (formulaImplementation.predicateOrNull(
                            formulaBuilder,
                            args,
                        )!! to globalIndexAfterWhitespaceAfterTerm)
                }
        }

    private fun <T : Formula> String.parseSpecial(
        globalIndexAfterAtomAndWhitespace: Int,
        factory: () -> T,
    ): Pair<T, Int>? =
        takeIf { get(globalIndexAfterAtomAndWhitespace) == ')' }
            ?.let {
                substring(globalIndexAfterAtomAndWhitespace + 1)
                    .indexOfFirstNonWhitespace()
                    .let { indexOfFirstNonWhitespaceAfterFormula ->
                        factory() to
                                indexOfFirstNonWhitespaceAfterFormula +
                                globalIndexAfterAtomAndWhitespace + 1
                    }
            }

}
