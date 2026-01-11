package bps.ipr.parser.ipr

import bps.ipr.parser.TermParser
import bps.ipr.parser.ipr.IprWhitespaceParser.Companion.indexOfFirstNonWhitespace
import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation

private val atomInitial: Set<Char> = (('a'..'z') + ('A'..'Z')).toSet() + '<' + '>' + '='
private val termInitial: Set<Char> = atomInitial + '('
private val atomNonInitial: Set<Char> = atomInitial + '-' + '_' + ('0'..'9').toSet() + '*'

interface IprFofTermParser : TermParser {

    val whitespaceParser: IprWhitespaceParser

    override fun String.parseArgumentsOrNull(startIndex: Int): Pair<List<Term>, Int>? =
        parseIprFofArgumentsOrNullHelper(startIndex)

    /**
     * expects the receiver to be a non-empty, comma-separated list of TPTP FOF terms (with no initial open paren)
     * followed by a closed paren then, possibly, other stuff.
     * @return the [Pair] of
     * 1. the [List] of [Term]s
     * 2. the index within the receiver of the `')'` that terminates the list of terms
     * or `null` if the receiver is not in the expected format including if an expected final closed paren is not found
     * @throws bps.ipr.terms.ArityOverloadException if a symbol is used with an arity that differs from the arity of that symbol
     * in the language
     */
    private fun String.parseIprFofArgumentsOrNullHelper(
        startIndex: Int,
        list: MutableList<Term> = mutableListOf(),
    ): Pair<List<Term>, Int>? =
        parseTermOrNull(startIndex)
            ?.let { (firstTerm: Term, indexAfterFirstTerm: Int) ->
                list.add(firstTerm)
                getOrNull(indexAfterFirstTerm)
                    ?.let { delimiter: Char ->
                        // NOTE need to say return here to prevent the ?: below from activating on a null value
                        return when (delimiter) {
                            ')' ->
                                list to indexAfterFirstTerm
                            else ->
                                parseIprFofArgumentsOrNullHelper(indexAfterFirstTerm, list)
                        }
                    }
            }
            ?: takeIf { length > startIndex && get(startIndex) == ')' }
                ?.let { emptyList<Term>() to startIndex }

    override fun String.parseTermOrNull(startIndex: Int): Pair<Term, Int>? =
        takeIf { length > startIndex }
            ?.let { get(startIndex) }
            ?.takeIf { it in termInitial }
            ?.let { initial: Char ->
                when (initial) {
                    '(' ->
                        parseAtomOrNull(startIndex + 1)
                            ?.let { (functor: String, nextIndex: Int) ->
                                parseArgumentsOrNull(nextIndex)
                                    ?.let { (args: List<Term>, closedParenInArgumentsInputIndex: Int) ->
                                        val globalIndexAfterClosedParen = closedParenInArgumentsInputIndex + 1
                                        val globalIndexAfterWhitespaceAfterTerm =
                                            indexOfFirstNonWhitespace(globalIndexAfterClosedParen)
                                        with(whitespaceParser) {
                                            takeIf { args.isEmpty() }
                                                ?.let {
                                                    return termImplementation.constantForSymbol(functor) to
                                                            globalIndexAfterWhitespaceAfterTerm
                                                }
                                                ?: (termImplementation
                                                    .properFunction(
                                                        termImplementation.functorForSymbol(functor, args.size),
                                                        args,
                                                    ) to globalIndexAfterWhitespaceAfterTerm)
                                        }
                                    }
                            }
                    else ->
                        // proper function
                        parseAtomOrNull(startIndex)
                            ?.let { (atom: String, index: Int) ->
                                termImplementation.freeVariableForSymbol(atom) to index
                            }
                }
            }

    /**
     * @return a [Pair] of the first atom in the receiver and the number of characters consumed
     * (including trailing whitespace) or `null` if the first non-whitespace part of the receiver is not an atom.
     */
    fun String.parseAtomOrNull(startIndex: Int): Pair<String, Int>? {
        return takeIf { length > startIndex }
            ?.let {
                beforeFirstMiss(startIndex, atomNonInitial)
                    .let { atom: String ->
                        (atom.length + startIndex)
                            .let { indexAfterAtom: Int ->
                                if (length == indexAfterAtom)
                                    atom to indexAfterAtom
                                else {
                                    val indexAfterWhitespaceAfterAtom: Int =
                                        indexOfFirstNonWhitespace(indexAfterAtom)
                                    if (indexAfterWhitespaceAfterAtom < length)
                                        if (get(indexAfterWhitespaceAfterAtom) in (atomInitial + ')' + '('))
                                            atom to indexAfterWhitespaceAfterAtom
                                        else
                                        // term parsing ends with an invalid character
                                            null
                                    else
                                        atom to length
                                }
                            }

                    }
            }
    }

    companion object {

        operator fun invoke(termImplementation: TermImplementation): IprFofTermParser =
            object : IprFofTermParser {
                override val termImplementation: TermImplementation = termImplementation
                override val whitespaceParser: IprWhitespaceParser = IprWhitespaceParser
            }

    }

}
