package bps.ipr.parser.tptp

import bps.ipr.formulas.FolFormula
import bps.ipr.formulas.FolFormulaImplementation
import bps.ipr.parser.FolFormulaParser
import bps.ipr.parser.TermParser
import bps.ipr.parser.WhitespaceParser

private val _tptpUpperAlpha = ('A'..'Z').toSet()
private val _tptpLowerAlpha = ('a'..'z').toSet()
private val _tptpNumeric = ('0'..'9').toSet()
private val _tptpAlphaNumeric = (_tptpUpperAlpha + _tptpLowerAlpha + _tptpNumeric).toSet()
private val _delimiters = setOf(',', '(', ')', '[', ']')

/**
 * Parse according to https://tptp.org/UserDocs/TPTPLanguage/SyntaxBNF.html#fof_formula
 */
interface TptpFofFormulaParser : FolFormulaParser {

//    val tptpUpperAlpha: Set<Char> get() = _tptpUpperAlpha
//    val tptpLowerAlpha: Set<Char> get() = _tptpLowerAlpha
//    val tptpAlphaNumeric: Set<Char> get() = _tptpAlphaNumeric

//    val delimiters: Set<Char> get() = _delimiters
//    override val termImplementation: TermImplementation = formulaImplementation.termImplementation

    /**
     * Attempts to parse the string as a term and returns a pair containing the parsed term
     * and the position in the string where parsing stopped.  Trailing whitespace should be consumed.
     *
     * @return a pair consisting of a term and the index where parsing stopped, or null if parsing fails.
     */
    // NOTE just doing TPTP fof_logic_formula for now
    /*
     %----FOF formulae.
<fof_formula>          ::= <fof_logic_formula> // BPS excluding for now: | <fof_sequent>
<fof_logic_formula>    ::= <fof_binary_formula> | <fof_unary_formula> | <fof_unitary_formula>
<fof_binary_formula>   ::= <fof_binary_nonassoc> | <fof_binary_assoc>
%----Only some binary connectives are associative
%----There's no precedence among binary connectives
<fof_binary_nonassoc>  ::= <fof_unit_formula> <nonassoc_connective> <fof_unit_formula>
<fof_binary_assoc>     ::= <fof_or_formula> | <fof_and_formula>
<fof_or_formula>       ::= <fof_unit_formula> <vline> <fof_unit_formula> |
                           <fof_or_formula> <vline> <fof_unit_formula>
<fof_and_formula>      ::= <fof_unit_formula> & <fof_unit_formula> |
                           <fof_and_formula> & <fof_unit_formula>
<fof_unary_formula>    ::= <unary_connective> <fof_unit_formula> | <fof_infix_unary>
 <fof_infix_unary>      ::= <fof_term> <infix_inequality> <fof_term>
%----<fof_unitary_formula> are in ()s or do not have a connective
<fof_unit_formula>     ::= <fof_unitary_formula> | <fof_unary_formula>
<fof_unitary_formula>  ::= <fof_quantified_formula> | <fof_atomic_formula> | (<fof_logic_formula>)
 %----All variables must be quantified
<fof_quantified_formula> ::= <fof_quantifier> [<fof_variable_list>] : <fof_unit_formula>
<fof_variable_list>    ::= <variable> | <variable>,<fof_variable_list>
<fof_atomic_formula>   ::= <fof_plain_atomic_formula> | <fof_defined_atomic_formula> |
                           <fof_system_atomic_formula>
<fof_plain_atomic_formula> ::= <fof_plain_term>
<fof_plain_atomic_formula> :== <proposition> | <predicate>(<fof_arguments>)
 %----Arguments recurse back to terms (this is the FOF world here)
<fof_arguments>        ::= <fof_term> | <fof_term>,<fof_arguments>
 %----For all language types
<atom>                 ::= <untyped_atom> | <defined_constant>
<untyped_atom>         ::= <constant> | <system_constant>

<proposition>          :== <predicate>
<predicate>            :== <atomic_word> // BPS: this is a lower_word
     */
    override fun String.parseFormulaOrNull(): Pair<FolFormula<*>, Int>? = TODO()
//        firstOfOrNull(delimiters)
//            ?.let { (firstDelimiter, indexOfFirstDelimiter) ->
//                // NOTE need to say return here to prevent the ?: below from activating on a null value
//                return when (firstDelimiter) {
//                    ',', ')' -> {
//                        // constant or variable or invalid
//                        substring(0, indexOfFirstDelimiter)
//                            .let { functor ->
//                                (functor.parseTptpVariableOrNull() ?: functor.parseTptpConstantOrNull())
//                                    ?.let { it to indexOfFirstDelimiter }
//                            }
//                    }
//                    '(' -> {
//                        // proper function
//                        substring(0, indexOfFirstDelimiter)
//                            .parseTptpFunctorOrNull()
//                            ?.let { functor ->
//                                val startIndexOfArguments = indexOfFirstDelimiter + 1
//                                substring(startIndexOfArguments)
//                                    .let { argumentsInput ->
//                                        argumentsInput
//                                            .parseTptpFofArgumentsOrNull()
//                                            ?.let { (args: List<Term>, closedParenInArgumentsInputIndex: Int) ->
//                                                val globalIndexAfterClosedParen =
//                                                    startIndexOfArguments + closedParenInArgumentsInputIndex + 1
//                                                formulaImplementation.termImplementation.properFunctionOrNull(
//                                                    functor,
//                                                    args,
//                                                )!! to
//                                                        substring(globalIndexAfterClosedParen)
//                                                            .indexOfFirstNonWhitespace() + globalIndexAfterClosedParen
//                                            }
//                                    }
//                            }
//                    }
//                    else -> {
//                        throw IllegalStateException()
//                    }
//                }
//            }
//            ?: run {
//                val tokenStartIndex = indexOfFirstNonWhitespace()
//                val tokenEndIndex = substring(tokenStartIndex).indexOfFirstWhitespace() + tokenStartIndex
//                val endOfFollowingWhitespace = substring(tokenEndIndex).indexOfFirstNonWhitespace() + tokenEndIndex
//                substring(tokenStartIndex, tokenEndIndex)
//                    .let { token ->
//                        (token.parseTptpVariableOrNull() ?: token.parseTptpConstantOrNull())
//                            ?.let { it to endOfFollowingWhitespace }
//                    }
//            }

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
//        formulaImplementation.termImplementation.parseTermOrNull()
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
//
//    /**
//     * @return the first found [Char] and its first index or `null` if none are present.
//     */
//    fun String.firstOfOrNull(stopAt: Collection<Char>): Pair<Char, Int>? {
//        forEachIndexed { index: Int, char: Char ->
//            if (char in stopAt) {
//                return char to index
//            }
//        }
//        return null
//    }
//
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
//            ?.let { formulaImplementation.termImplementation.constantOrNull(it) }
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

        operator fun invoke(formulaImplementation: FolFormulaImplementation): TptpFofFormulaParser =
            object : TptpFofFormulaParser, TermParser by TptpFofTermParser(formulaImplementation.termImplementation) {
                override val formulaImplementation: FolFormulaImplementation = formulaImplementation
                override val whitespaceParser: WhitespaceParser = TptpWhitespaceParser
            }

    }

}
