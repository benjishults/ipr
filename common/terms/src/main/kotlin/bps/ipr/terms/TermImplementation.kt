package bps.ipr.terms

import bps.kotlin.allIndexed

/**
 * A [TermImplementation] is a factory for [Term]s in a given language.  Different implementations might return
 * [Term]s with different internal structures.  This is useful, for example, when say a structure-conserving DAG
 * representation is useful sometimes but not other times.
 */
interface TermImplementation : AutoCloseable {

    /**
     * The [TermLanguage] used to determine the normalizations of symbols.
     */
    val termLanguage: TermLanguage

    /**
     * This method is not idempotent.  A [TermImplementation] may be meaningfully reused and re-closed any number of
     * times.  However, it is recommended to simply create a new one after one is closed.
     */
    override fun close() =
        clear()

    fun clear() {
        termLanguage.clear()
    }

//    /**
//     * @return `true` if we can determine in constant time that the two terms are identical
//     */
//    fun identical(term1: Term, term2: Term): Boolean =
//        term1 === term2

    /**
     * @return a [Variable] for the normalization of the given [symbol] or `null` if that isn't possible.
     */
    fun variableOrNull(symbol: String): Variable?

    /**
     * @return a [Constant] for the normalization of the given [symbol] as a functor or `null` if that isn't possible.
     */
    fun constantOrNull(symbol: String): Constant?

    /**
     * @return a [ProperFunction] for the normalization of the given [symbol] as a functor and the
     * given arguments or `null` if that isn't possible.
     */
    fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction?

    /**
     * Allows absolutely everything and interns nothing.
     */
    companion object : TermImplementation {
        override val termLanguage: TermLanguage = TermLanguage

        override fun variableOrNull(symbol: String): Variable? =
            FreeVariable(symbol)

        override fun constantOrNull(symbol: String): Constant? =
            Constant(symbol)

        override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
            ProperFunction(symbol, ArgumentList(arguments))
    }

}

open class FolTermImplementation(
    override val termLanguage: TermLanguage = FolTermLanguage(),
) : TermImplementation {

    protected val variableInternTable = mutableMapOf<String, FreeVariable>()
    protected val constantInternTable = mutableMapOf<String, Constant>()

    override fun clear() {
        super.clear()
        variableInternTable.clear()
        constantInternTable.clear()
    }

    override fun variableOrNull(symbol: String): Variable? =
        termLanguage
            .toNormalizedVariableOrNull(symbol)
            ?.let {
                variableInternTable.getOrPut(it) { FreeVariable(it) }
            }

    override fun constantOrNull(symbol: String): Constant? =
        termLanguage
            .toNormalizedFunctorOrNull(symbol, 0)
            ?.let {
                constantInternTable.getOrPut(symbol) { Constant(symbol) }
            }

    override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
        termLanguage
            .toNormalizedFunctorOrNull(symbol, arguments.size)
            ?.let {
                ProperFunction(symbol, ArgumentList(arguments))
            }

}

open class FolDagTermImplementation(
    termLanguage: TermLanguage = FolTermLanguage(),
) : FolTermImplementation(termLanguage) {

    // NOTE ArgumentLists can't be put into sets or used as keys in maps so this is what I've got.  :(
    // TODO consider clearing this once parsing is done just to give back some memory
    protected val properFunctionInternTable = mutableMapOf<String, MutableList<Pair<ArgumentList, ProperFunction>>>()

    override fun clear() {
        super.clear()
        properFunctionInternTable.clear()
    }

    override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
        termLanguage
            .toNormalizedFunctorOrNull(symbol, arguments.size)
            ?.let {
                properFunctionInternTable[symbol]
                    ?.let { listOfTermsWithSymbol ->
                        listOfTermsWithSymbol
                            .find { (args: ArgumentList, _) ->
                                arguments.allIndexed { index, termInArguments: Term ->
                                    termInArguments === args[index]
                                }
                            }
                            ?.second
                            ?: ArgumentList(arguments)
                                .let { args ->
                                    ProperFunction(
                                        symbol,
                                        args,
                                    )
                                        .also { listOfTermsWithSymbol.add(args to it) }
                                }
                    }
                    ?: ProperFunction(symbol, ArgumentList(arguments))
                        .also { termToReturn ->
                            properFunctionInternTable[symbol] =
                                mutableListOf(termToReturn.arguments to termToReturn)
                        }
            }

}
