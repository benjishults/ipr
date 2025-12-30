package bps.ipr.terms

import bps.kotlin.allIndexed

/**
 * A [TermImplementation] is a factory for [Term]s in a given language.  Different implementations might return
 * [Term]s with different internal structures.  This is useful, for example, when say a structure-conserving DAG
 * representation is useful sometimes but not other times.
 */
interface TermImplementation : AutoCloseable {

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

    /**
     * @return a [FreeVariable] for something close to the normalization of the given [symbol].
     * Throws an exception if normalization isn't possible.
     */
    fun newFreeVariable(symbol: String): FreeVariable

    /**
     * @return a [FreeVariable] for the normalization of the given [symbol].
     * Throws an exception if normalization isn't possible.
     */
    fun freeVariableForSymbol(symbol: String): FreeVariable

//    /**
//     * @return a [BoundVariable] for the normalization of the given [symbol] or `null` if that isn't possible.
//     */
//    fun boundVariableOrNull(symbol: String): BoundVariable?

    /**
     * @return a [Constant] for the normalization of the given [symbol].
     * Throws an exception if normalization isn't possible.
     */
    fun constantForSymbol(symbol: String): Constant

    fun functorForSymbol(symbol: String):

    /**
     * @return a [ProperFunction] for the normalization of the given [symbol] and the
     * given arguments.
     */
    fun properFunction(symbol: String, arguments: List<Term>): ProperFunction

    /**
     * Allows absolutely everything and interns nothing.
     */
    companion object : TermImplementation {
        override val termLanguage: TermLanguage = TermLanguage
        override fun newFreeVariable(symbol: String): FreeVariable {
            TODO("Not yet implemented")
        }

        override fun freeVariableForSymbol(symbol: String): FreeVariable =
            FreeVariable(symbol)

//        override fun boundVariableOrNull(symbol: String): BoundVariable =
//            BoundVariable(symbol)

        override fun constantForSymbol(symbol: String): Constant =
            Constant(symbol)

        override fun properFunction(symbol: String, arguments: List<Term>): ProperFunction =
            ProperFunction(symbol, ArgumentList(arguments))
    }

}

/**
 * This class is not thread-safe.
 */
open class FolTermImplementation(
    override val termLanguage: TermLanguage = FolTermLanguage(),
) : TermImplementation {

    /**
     * maps normalized symbols to [FreeVariable]s
     */
    protected val freeVariableInternTable = mutableMapOf<String, FreeVariable>()

    protected val freeVariableSymbolGenerationInfo = mutableMapOf<String, Long>()

    //        protected val boundVariableInternTable = mutableMapOf<String, MutableList<BoundVariable>>()
    /**
     * maps normalized symbols to [Constant]s
     */
    protected val constantInternTable = mutableMapOf<String, Constant>()

    override fun clear() {
        super.clear()
        freeVariableInternTable.clear()
//        boundVariableInternTable.clear()
        constantInternTable.clear()
    }

    /**
     * only called when [normalized] is known to be on the [freeVariableInternTable] already.
     */
    protected open fun nextNormalizedFreeVariable(normalized: String): FreeVariable =
        (freeVariableSymbolGenerationInfo[normalized] ?: -1)
            .let { lastSuccessfulSuffix: Long ->
                var nextSuffix = lastSuccessfulSuffix + 1L
                var currentAttempt = "${normalized}_${nextSuffix}"
                while (freeVariableInternTable.containsKey(currentAttempt)) {
                    nextSuffix++
                    currentAttempt = "${normalized}_${nextSuffix}"
                }
                FreeVariable(currentAttempt)
                    .also {
                        freeVariableInternTable[currentAttempt] = it
                        freeVariableSymbolGenerationInfo[normalized] = nextSuffix
                    }
            }

    /**
     * Operates in a referentially transparent way.
     */
    override fun freeVariableForSymbol(symbol: String): FreeVariable =
        termLanguage
            .toNormalizedFreeVariableOrNull(symbol)!!
            .let {
                freeVariableInternTable.getOrPut(it) { FreeVariable(it) }
            }

    override fun newFreeVariable(symbol: String): FreeVariable =
        termLanguage
            .toNormalizedFreeVariableOrNull(symbol)!!
            .let { initialNormalization ->
                if (freeVariableInternTable.containsKey(initialNormalization)) {
                    nextNormalizedFreeVariable(initialNormalization)
                } else
                    FreeVariable(initialNormalization)
                        .also {
                            freeVariableInternTable[initialNormalization] = it
                        }
            }


//    override fun boundVariableOrNull(symbol: String): BoundVariable? =
//        termLanguage
//            .toNormalizedBoundVariableOrNull(symbol)
//            ?.let { normalizedSymbol ->
//                boundVariableInternTable
//                    .getOrPut(normalizedSymbol) {
//                        mutableListOf()
//                    }
//                    .let { existingList ->
//                        BoundVariable(normalizedSymbol, existingList.size.toLong())
//                            .apply {
//                                existingList.add(this)
//                            }
//                    }
//            }

    /**
     * Operates in a referentially transparent way.
     */
    override fun constantForSymbol(symbol: String): Constant =
        termLanguage
            .toNormalizedFunctorOrNull(symbol, 0)!!
            .let {
                constantInternTable.getOrPut(it) { Constant(it) }
            }
getSkolemSymbol
    override fun properFunction(symbol: String, arguments: List<Term>): ProperFunction =
        ProperFunction(
            symbol =
                termLanguage
                    .toNormalizedFunctorOrNull(symbol, arguments.size)!!,
            arguments = ArgumentList(arguments),
        )

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

    // TODO if this is referentially transparent, then let's document that.  (I think it should be)
    override fun properFunction(symbol: String, arguments: List<Term>): ProperFunction =
        termLanguage
            .toNormalizedFunctorOrNull(symbol, arguments.size)!!
            .let {
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
                                        symbol = symbol,
                                        arguments = args,
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
