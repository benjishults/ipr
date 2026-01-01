package bps.ipr.parser.tptp

import bps.ipr.parser.TermParser
import bps.ipr.terms.Constant
import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation
import bps.ipr.terms.Variable

private val _tptpUpperAlpha = ('A'..'Z').toSet()
private val _tptpLowerAlpha = ('a'..'z').toSet()
private val _tptpNumeric = ('0'..'9').toSet()
private val _tptpAlphaNumeric = (_tptpUpperAlpha + _tptpLowerAlpha + _tptpNumeric).toSet()
private val _delimiters = setOf(',', '(', ')')

/**
 * Parse according to https://tptp.org/UserDocs/TPTPLanguage/SyntaxBNF.html#fof_term
 */
interface TptpFofTermParser : TermParser {

    val whitespaceParser: TptpWhitespaceParser
    val tptpUpperAlpha: Set<Char> get() = _tptpUpperAlpha
    val tptpLowerAlpha: Set<Char> get() = _tptpLowerAlpha
    val tptpAlphaNumeric: Set<Char> get() = _tptpAlphaNumeric

    val termDelimiters: Set<Char> get() = _delimiters

    override fun String.parseTermOrNull(startIndex: Int): Pair<Term, Int>? =
        firstOfOrNull(startIndex, termDelimiters)
            ?.let { (firstDelimiter, indexOfFirstDelimiter) ->
                // NOTE need to say return here to prevent the ?: below from activating on a null value
                return when (firstDelimiter) {
                    ',', ')' -> {
                        // constant or variable or invalid
                        substring(startIndex, indexOfFirstDelimiter)
                            .let { functor ->
                                (functor.parseTptpVariableOrNull() ?: functor.parseTptpConstantOrNull())
                                    ?.let { it to indexOfFirstDelimiter }
                            }
                    }
                    '(' -> {
                        // proper function
                        substring(startIndex, indexOfFirstDelimiter)
                            .parseTptpFunctorOrNull()
                            ?.let { functor: String ->
                                val startIndexOfArguments = indexOfFirstDelimiter + 1
                                substring(startIndexOfArguments)
                                    .parseArgumentsOrNull(startIndex)
                                    ?.let { (args: List<Term>, closedParenInArgumentsInputIndex: Int) ->
                                        val globalIndexAfterClosedParen =
                                            startIndexOfArguments + closedParenInArgumentsInputIndex + 1
                                        with(whitespaceParser) {
                                            termImplementation
                                                .properFunction(
                                                    termImplementation.functorForSymbol(functor, args.size),
                                                    args,
                                                ) to
                                                    substring(globalIndexAfterClosedParen)
                                                        .indexOfFirstNonWhitespace() + globalIndexAfterClosedParen
                                        }
                                    }
                            }
                    }
                    else -> {
                        // FIXME should this be null?
                        throw IllegalStateException()
                    }
                }
            }
            ?: run {
                with(whitespaceParser) {
                    val tokenStartIndex = indexOfFirstNonWhitespace()
                    val tokenEndIndex = substring(tokenStartIndex).indexOfFirstWhitespace() + tokenStartIndex
                    val endOfFollowingWhitespace = substring(tokenEndIndex).indexOfFirstNonWhitespace() + tokenEndIndex
                    substring(tokenStartIndex, tokenEndIndex)
                        .let { token ->
                            (token.parseTptpVariableOrNull() ?: token.parseTptpConstantOrNull())
                                ?.let { it to endOfFollowingWhitespace }
                        }
                }
            }

    /**
     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms followed by a closed paren.
     * @return the [Pair] of
     * 1. the [List] of [Term]s
     * 2. the index within the receiver of the closed paren terminating the list of arguments
     *
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     */
    // TODO since this only returns ')' as the delimiter, we shouldn't need to use the DelimiterAndIndex class here.
    override fun String.parseArgumentsOrNull(startIndex: Int): Pair<List<Term>, Int>? =
        parseTptpFofArgumentsOrNullHelper(startIndex)

    /**
     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms (with no initial open paren)
     * followed by a closed paren then, possibly, other stuff.
     * @return the [Pair] of
     * 1. the [List] of [Term]s
     * 2. the index within the receiver of the `')'` that terminates the list of terms
     *
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     */
    private fun String.parseTptpFofArgumentsOrNullHelper(
        startIndex: Int,
        list: MutableList<Term> = mutableListOf(),
    ): Pair<List<Term>, Int>? =
        parseTermOrNull(startIndex)
            ?.let { (firstTerm: Term, indexAfterFirstTerm: Int) ->
                list.add(firstTerm)
                getOrNull(indexAfterFirstTerm)
                    ?.let { delimiter ->
                        // NOTE need to say return here to prevent the ?: below from activating on a null value
                        return when (delimiter) {
                            ',' -> {
                                substring(indexAfterFirstTerm + 1)
                                    .parseTptpFofArgumentsOrNullHelper(0, list)
                                    ?.let { (_, indexOfCloseParenAfterAllArguments) ->
                                        list to indexOfCloseParenAfterAllArguments + (indexAfterFirstTerm + 1)
                                    }
                            }
                            ')' -> {
                                list to indexAfterFirstTerm
                            }
                            else -> {
                                null
                            }
                        }
                    }
                    ?: run {
                        // exhausted input on first term
                        list to length
                    }
            }

    fun String.isTptpLowerWord(): Boolean =
        isNotEmpty() &&
                get(0) in tptpLowerAlpha &&
                substring(1)
                    .all { it in tptpAlphaNumeric }

    fun String.isTptpUpperWord(): Boolean =
        isNotEmpty() &&
                get(0) in tptpUpperAlpha &&
                substring(1)
                    .all { it in tptpAlphaNumeric }

    // TODO make these work like the rest
    fun String.parseTptpConstantOrNull(): Constant? =
        trim()
            .takeIf { it.isTptpLowerWord() }
            ?.let { termImplementation.constantForSymbol(it) }

    // TODO make these work like the rest
    fun String.parseTptpVariableOrNull(): Variable? =
        trim()
            .takeIf { it.isTptpUpperWord() }
            ?.let { termImplementation.freeVariableForSymbol(it) }

    // TODO make these work like the rest
    fun String.parseTptpFunctorOrNull(): String? =
        trim()
            .takeIf { it.isTptpLowerWord() }

    companion object {

        operator fun invoke(termImplementation: TermImplementation): TptpFofTermParser =
            object : TptpFofTermParser {
                override val termImplementation: TermImplementation = termImplementation
                override val whitespaceParser: TptpWhitespaceParser = TptpWhitespaceParser
            }

    }

}
