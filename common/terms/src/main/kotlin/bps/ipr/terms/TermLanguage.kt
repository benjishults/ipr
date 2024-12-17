package bps.ipr.terms

import bps.kotlin.allIndexed
import bps.kotlin.findIndexedOrNull

/**
 * A [TermLanguage] controls
 * 1. what symbol is allowed to be a variable
 * 2. what symbol is allowed to be a functor symbol
 * 3. the allowed arity of a given functor symbol
 *
 * A term language can be very liberal, allowing anything to be anything and returning new structure on every call.
 * Or it can be very controlling of arity and conserve structure as much as possible.
 */
interface TermLanguage {

    /**
     * Clear the state of the language.
     */
    fun clear(): Unit {}

    /**
     * @return `null` if the symbol is not normalizable (not allowed to be a variable), otherwise, the normalized
     * variable symbol.
     */
    fun String.normalizeVariableOrNull(): String? = this

    /**
     * @return `null` if the symbol is not normalizable (not allowed to be a functor), otherwise, the normalized
     * functor symbol.
     */
    fun String.normalizeFunctorOrNull(): String? = this

    /**
     * @return a [Variable] for normalization of the given [symbol] or `null` if that isn't possible.
     */
    fun variableOrNull(symbol: String): Variable?

    /**
     * @return a [Constant] for normalization of the given [symbol] as a functor or `null` if that isn't possible.
     */
    fun constantOrNull(symbol: String): Constant?

    /**
     * @return a [ProperFunction] for the normalization of the given [symbol] as a functor and the
     * given arguments or `null` if that isn't possible.
     */
    fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction?

    /**
     * @return `true` if the two [Term]s are syntactically identical.
     */
    fun equalTerms(term1: Term, term2: Term): Boolean =
        when (term1) {
            is Variable -> term2 is Variable &&
                    equalVariables(term1, term2)
            is Constant -> term2 is Constant &&
                    equalConstants(term1, term2)
            is ProperFunction -> term2 is ProperFunction &&
                    equalProperFunctions(term1, term2)
        }

    fun equalVariables(variable1: Variable, variable2: Variable): Boolean =
        variable1 == variable2

    fun equalConstants(constant1: Constant, constant2: Constant): Boolean =
        constant1 == constant2

    fun equalProperFunctions(properFunction1: ProperFunction, properFunction2: ProperFunction): Boolean =
        properFunction1 == properFunction2

}

/**
 * Keeps variables and constants on intern tables and normalizes only by trimming external whitespace.
 */
open class FolTermLanguage : TermLanguage {

    protected val variableInternTable = mutableMapOf<String, FreeVariable>()
    protected val arityInternTable = mutableMapOf<String, Int>()
    protected val constantInternTable = mutableMapOf<String, Constant>()

    override fun clear() {
        variableInternTable.clear()
        arityInternTable.clear()
        constantInternTable.clear()
    }

    /**
     * @return `null` if the symbol is not normalizable (not allowed to be a variable)
     */
    override fun String.normalizeVariableOrNull(): String? = this.trim()

    /**
     * @return `null` if the symbol is not normalizable (not allowed to be a functor)
     */
    override fun String.normalizeFunctorOrNull(): String? = this.trim()

    override fun variableOrNull(symbol: String): Variable? =
        symbol.normalizeVariableOrNull()
            ?.let {
                variableInternTable.getOrPut(it) { FreeVariable(it) }
            }

    override fun constantOrNull(symbol: String): Constant? =
        symbol.normalizeFunctorOrNull()
            ?.let {
                arityInternTable.getOrPut(symbol) { 0 }
                    .takeIf { it == 0 }
                    ?.run { constantInternTable.getOrPut(symbol) { Constant(symbol) } }
            }

    override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
        symbol.normalizeFunctorOrNull()
            ?.let {
                arityInternTable.getOrPut(symbol) { arguments.size }
                    .takeIf { it == arguments.size }
                    ?.run {
                        ProperFunction(symbol, ArgumentList(arguments))
                    }
            }

    /**
     * Uses the identity since they are always interned.
     */
    override fun equalConstants(constant1: Constant, constant2: Constant): Boolean =
        constant1 === constant2

    /**
     * Uses the identity since they are always interned.
     */
    override fun equalVariables(variable1: Variable, variable2: Variable): Boolean =
        variable1 === variable2

}

open class FolDagTermLanguage : FolTermLanguage() {
    protected val properFunctionInternTable = mutableMapOf<String, MutableList<Pair<ArgumentList, ProperFunction>>>()

    override fun clear() {
        super.clear()
        properFunctionInternTable.clear()
    }

    override fun properFunctionOrNull(symbol: String, arguments: List<Term>): ProperFunction? =
        arityInternTable
            .getOrPut(symbol) { arguments.size }
            .takeIf { it == arguments.size }
            ?.run {
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

    /**
     * Uses the identity since they are always interned.
     */
    override fun equalProperFunctions(properFunction1: ProperFunction, properFunction2: ProperFunction): Boolean =
        properFunction1 === properFunction2

}
