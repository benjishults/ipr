package bps.ipr.parser.ipr

import bps.ipr.parser.TermParser
import bps.ipr.parser.ipr.IprWhitespaceParser.Companion.indexOfFirstNonWhitespace
import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation

private val atomInitial = ('a'..'z').toSet()
private val termInitial = atomInitial + '('
private val atomNonInitial = atomInitial + '-' + '_' + ('0'..'9').toSet()

interface IprFofTermParser : TermParser {

    val whitespaceParser: IprWhitespaceParser

    override fun String.parseArgumentsOrNull(): Pair<List<Term>, Int>? =
        TODO()

    override fun String.parseTermOrNull(): Pair<Term, Int>? =
        firstOfOrNull(termInitial)
            ?.let { (termInitial: Char, indexOfTermInitial: Int) ->
                if (0 != indexOfTermInitial)
                    null
                else
                    when (termInitial) {
                        '(' ->
                            // constant or function or invalid
                            substring(1)
                                .parseAtomOrNull()
                                ?.let { (functor: String, nextIndex: Int) ->
                                    // TODO parse arguments and build function or constant
                                    val startIndexOfArguments: Int = 1 + nextIndex
                                    substring(startIndexOfArguments)
                                        .parseArgumentsOrNull()
                                        ?.let { (args: List<Term>, closedParenInArgumentsInputIndex: Int) ->
                                            val globalIndexAfterClosedParen =
                                                startIndexOfArguments + closedParenInArgumentsInputIndex + 1
                                            with(whitespaceParser) {
                                                termImplementation.properFunctionOrNull(functor, args)!! to
                                                        substring(globalIndexAfterClosedParen)
                                                            .indexOfFirstNonWhitespace() + globalIndexAfterClosedParen
                                            }
                                        }
                                }
                        else ->
                            // proper function
                            parseAtomOrNull()
                                ?.let { (atom: String, index: Int) ->
                                    termImplementation.freeVariableOrNull(atom)
                                        ?.let { variable -> variable to index }
                                }
                    }
            }

    /**
     * @return a [Pair] of the first atom in the receiver and the number of characters consumed
     * (including trailing whitespace) or `null` if the first non-whitespace part of the receiver is not an atom.
     */
    private fun String.parseAtomOrNull(): Pair<String, Int>? =
        firstOfOrNull(atomInitial)
            ?.let { (_, indexOfFirstChar) ->
                // ensure everything skipped is whitespace
                if (substring(0, indexOfFirstChar).indexOfFirstNonWhitespace() != indexOfFirstChar)
                    null
                else
                    substring(indexOfFirstChar)
                        .beforeFirstMiss(atomNonInitial)
                        .let { atom: String ->
                            (atom.length + indexOfFirstChar)
                                .let { fullLength: Int ->
                                    if (length == fullLength)
                                        atom to fullLength
                                    else {
                                        val nonWhitespaceFollowerIndex: Int =
                                            substring(fullLength).indexOfFirstNonWhitespace() + fullLength
                                        if (nonWhitespaceFollowerIndex < length)
                                            if (get(nonWhitespaceFollowerIndex) in (atomInitial + ')'))
                                                atom to nonWhitespaceFollowerIndex
                                            else
                                            // term parsing ends with an invalid character
                                                null
                                        else
                                            atom to length
                                    }
                                }

                        }
            }

//    /**
//     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms followed by a closed paren.
//     * @return the [Pair] of
//     * 1. the [List] of [Term]s
//     * 2. the index within the receiver of the closed paren terminating the list of arguments
//     *
//     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
//     */
//    // TODO since this only returns ')' as the delimiter, we shouldn't need to use the DelimiterAndIndex class here.
//    fun String.parseTptpFofArgumentsOrNull(): Pair<List<Term>, Int>? =
//        parseTptpFofArgumentsOrNullHelper()
//            ?.let { (list, indexOfTerminatingClosedParen) ->
//                list.toList() to indexOfTerminatingClosedParen
//            }
//
//    /**
//     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms (with no initial open paren)
//     * followed by a closed paren then, possibly, other stuff.
//     * @return the [Pair] of
//     * 1. the [List] of [Term]s
//     * 2. the index within the receiver of the `')'` that terminates the list of terms
//     *
//     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
//     */
//    // TODO probably don't need to return the mutable list since it's just being mutated and the return value isn't used
//    private fun String.parseTptpFofArgumentsOrNullHelper(
//        list: MutableList<Term> = mutableListOf(),
//    ): Pair<MutableList<Term>, Int>? =
//        parseTermOrNull()
//            ?.let { (firstTerm: Term, indexAfterFirstTerm: Int) ->
//                list.add(firstTerm)
//                getOrNull(indexAfterFirstTerm)
//                    ?.let { delimiter ->
//                        // NOTE need to say return here to prevent the ?: below from activating on a null value
//                        return when (delimiter) {
//                            ',' -> {
//                                substring(indexAfterFirstTerm + 1)
//                                    .parseTptpFofArgumentsOrNullHelper(list)
//                                    ?.let { (_, indexOfCloseParenAfterAllArguments) ->
//                                        list to indexOfCloseParenAfterAllArguments + (indexAfterFirstTerm + 1)
//                                    }
//                            }
//                            ')' -> {
//                                list to indexAfterFirstTerm
//                            }
//                            else -> {
//                                null
//                            }
//                        }
//                    }
//                    ?: run {
//                        // exhausted input on first term
//                        list to length
//                    }
//            }

//    fun String.isTptpLowerWord(): Boolean =
//        isNotEmpty() &&
//                get(0) in tptpLowerAlpha &&
//                substring(1)
//                    .all { it in tptpAlphaNumeric }
//
//    fun String.isTptpUpperWord(): Boolean =
//        isNotEmpty() &&
//                get(0) in tptpUpperAlpha &&
//                substring(1)
//                    .all { it in tptpAlphaNumeric }
//
//    // TODO make these work like the rest
//    fun String.parseTptpConstantOrNull(): Constant? =
//        trim()
//            .takeIf { it.isTptpLowerWord() }
//            ?.let { termImplementation.constantOrNull(it) }
//
//    // TODO make these work like the rest
//    fun String.parseTptpVariableOrNull(): Variable? =
//        trim()
//            .takeIf { it.isTptpUpperWord() }
//            ?.let { termImplementation.freeVariableOrNull(it) }
//
//    // TODO make these work like the rest
//    fun String.parseTptpFunctorOrNull(): String? =
//        trim()
//            .takeIf { it.isTptpLowerWord() }

    companion object {

        operator fun invoke(termImplementation: TermImplementation): IprFofTermParser =
            object : IprFofTermParser {
                override val termImplementation: TermImplementation = termImplementation
                override val whitespaceParser: IprWhitespaceParser = IprWhitespaceParser
            }

    }

}
