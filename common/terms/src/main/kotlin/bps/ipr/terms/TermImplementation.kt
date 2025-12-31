package bps.ipr.terms

import bps.kotlin.allIndexed
import kotlin.collections.find

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
     * @return a [FreeVariable] for a symbol similar to [symbol] but new to the language.
     * Throws an exception if the language just can't allow it.
     */
    fun newFreeVariable(symbol: String): FreeVariable

    /**
     * @return a [FreeVariable] if the given [symbol] is allowed by the language as a variable.
     * Throws an exception otherwise.
     */
    fun freeVariableForSymbol(symbol: String): FreeVariable

//    /**
//     * @return a [BoundVariable] for the normalization of the given [symbol] or `null` if that isn't possible.
//     */
//    fun boundVariableOrNull(symbol: String): BoundVariable?

    /**
     * @return a [Constant] for the given [symbol] or throw an exception if the [symbol] isn't allowed as a constant.
     */
    fun constantForSymbol(symbol: String): Constant

    /**
     * Throws an exception if the given [symbol] isn't allowed at that arity in the given language.
     */
    fun functorForSymbol(symbol: String, arity: Int): Functor =
        Functor(
            termLanguage
                .registerFunctorOrNull(symbol, arity)!!,
        )

    /**
     * @return a [Functor] for a symbol similar to [symbol] but new to the language.
     * Throws an exception if the language just can't allow it.
     */
    fun newFunctorForSymbol(symbol: String, arity: Int): Functor

    /**
     * @return a [ProperFunction] for the given [functor] and the
     * given arguments.
     */
    fun properFunction(functor: Functor, arguments: Iterable<Term>): ProperFunction

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
            Constant(functorForSymbol(symbol, 0))

        override fun newFunctorForSymbol(symbol: String, arity: Int): Functor =
            TODO("Not yet implemented")

        override fun properFunction(functor: Functor, arguments: Iterable<Term>): ProperFunction =
            ProperFunction(functor, ArgumentList(arguments))
    }

}

/**
 * This class is not thread-safe.
 */
open class FolTermImplementation(
    override val termLanguage: FolTermLanguage = FolTermLanguage(),
) : TermImplementation {

    /**
     * maps symbols to [FreeVariable]s
     */
    protected val freeVariableInternTable = mutableMapOf<String, FreeVariable>()

    /**
     * Maps a string to the last successful number that was used to create a new variable from that symbol.
     */
    protected val freeVariableSymbolGenerationInfo = mutableMapOf<String, Long>()

    /**
     * maps symbols to [Constant]s
     */
    protected val constantInternTable = mutableMapOf<String, Constant>()

    //        protected val boundVariableInternTable = mutableMapOf<String, MutableList<BoundVariable>>()
//    /**
//     * maps normalized symbols to [Constant]s
//     */
//    protected val constantInternTable = mutableMapOf<String, Constant>()

    /**
     * maps a symbol to its existing [Functor].
     */
    // TODO is this really an improvement?  The trade of is lots of "duplicate" functors vs this table.
    protected val functorInternTable = mutableMapOf<String, Functor>()

    /**
     * Maps a string to the last successful number that was used to create a new variable from that symbol.
     */
    protected val functorSymbolGenerationInfo = mutableMapOf<String, Long>()

    override fun clear() {
        super.clear()
        freeVariableInternTable.clear()
        freeVariableSymbolGenerationInfo.clear()
        constantInternTable.clear()
//        boundVariableInternTable.clear()
        functorInternTable.clear()
        functorSymbolGenerationInfo.clear()
    }

    /**
     * only called when [symbol] is known to be on the [freeVariableInternTable] already.
     */
    protected open fun nextFreeVariable(symbol: String): FreeVariable =
        (freeVariableSymbolGenerationInfo[symbol] ?: -1)
            .let { lastSuccessfulSuffix: Long ->
                var nextSuffix = lastSuccessfulSuffix + 1L
                var currentAttempt = "${symbol}_${nextSuffix}"
                while (freeVariableInternTable.containsKey(currentAttempt)) {
                    nextSuffix++
                    currentAttempt = "${symbol}_${nextSuffix}"
                }
                FreeVariable(termLanguage.registerFreeVariableOrNull(currentAttempt)!!)
                    .also {
                        freeVariableInternTable[currentAttempt] = it
                        freeVariableSymbolGenerationInfo[symbol] = nextSuffix
                    }
            }

    /**
     * only called when [symbol] is known to be on the [functorInternTable] already.
     */
    protected open fun nextFunctor(symbol: String, arity: Int): Functor =
        (functorSymbolGenerationInfo[symbol] ?: -1)
            .let { lastSuccessfulSuffix: Long ->
                var nextSuffix = lastSuccessfulSuffix + 1L
                var currentAttempt = "${symbol}_${nextSuffix}"
                while (functorInternTable.containsKey(currentAttempt)) {
                    nextSuffix++
                    currentAttempt = "${symbol}_${nextSuffix}"
                }
                Functor(termLanguage.registerFunctorOrNull(currentAttempt, arity)!!)
                    .also {
                        functorInternTable[currentAttempt] = it
                        functorSymbolGenerationInfo[symbol] = nextSuffix
                    }
            }

    /**
     * Operates in a referentially transparent way.
     */
    override fun freeVariableForSymbol(symbol: String): FreeVariable =
        termLanguage
            .registerFreeVariableOrNull(symbol)!!
            .let {
                freeVariableInternTable.getOrPut(it) { FreeVariable(it) }
            }

    override fun newFreeVariable(symbol: String): FreeVariable =
        termLanguage
            .registerFreeVariableOrNull(symbol)!!
            .let { initialNormalization ->
                if (freeVariableInternTable.containsKey(initialNormalization)) {
                    nextFreeVariable(initialNormalization)
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

    override fun functorForSymbol(symbol: String, arity: Int): Functor =
        termLanguage.registerFunctorOrNull(symbol, arity)
            ?.let { registered: String ->
                functorInternTable.getOrPut(registered) {
                    Functor(termLanguage.registerFunctorOrNull(registered, arity)!!)
                }
            }
            ?: throw ArityOverloadException(
                "$symbol already has arity ${
                    termLanguage.arityInternTable[symbol]
                }",
                )

    override fun newFunctorForSymbol(symbol: String, arity: Int): Functor =
//        termLanguage
//            .registerFunctorOrNull(symbol, arity)
//            ?.let { initial: String ->
        if (functorInternTable.containsKey(symbol)) {
            nextFunctor(symbol, arity)
        } else
            Functor(termLanguage.registerFunctorOrNull(symbol, arity)!!)
                .also {
                    functorInternTable[symbol] = it
                }

    /**
     * Operates in a referentially transparent way.
     */
    override fun constantForSymbol(symbol: String): Constant =
        constantInternTable.getOrPut(symbol) {
            Constant(functorForSymbol(symbol, 0))
        }

    //    getSkolemSymbol
    override fun properFunction(functor: Functor, arguments: Iterable<Term>): ProperFunction =
        ProperFunction(functor, ArgumentList(arguments))

}

open class FolDagTermImplementation(
    termLanguage: FolTermLanguage = FolTermLanguage(),
) : FolTermImplementation(termLanguage) {

    // NOTE ArgumentLists can't be put into sets or used as keys in maps so this is what I've got.  :(
    protected val properFunctionInternTable = mutableMapOf<String, MutableList<Pair<ArgumentList, ProperFunction>>>()

    override fun clear() {
        super.clear()
        properFunctionInternTable.clear()
    }

    // TODO if this is referentially transparent, then let's document that.  (I think it is/should be)
    override fun properFunction(functor: Functor, arguments: Iterable<Term>): ProperFunction =
        properFunctionInternTable[functor.symbol]
            ?.let { listOfTermsWithSymbol: MutableList<Pair<ArgumentList, ProperFunction>> ->
                listOfTermsWithSymbol
                    .find { (args: ArgumentList, _) ->
                        arguments.allIndexed { index: Int, termInArguments: Term ->
                            termInArguments === args.elementAt(index)
                        }
                    }
                    ?.second
                    ?: ArgumentList(arguments)
                        .let { args ->
                            ProperFunction(
                                functor = functor,
                                arguments = args,
                            )
                                .also { listOfTermsWithSymbol.add(args to it) }
                        }
            }
            ?: ProperFunction(functor, ArgumentList(arguments))
                .also { termToReturn ->
                    properFunctionInternTable[functor.symbol] =
                        mutableListOf(termToReturn.arguments to termToReturn)
                }

}
