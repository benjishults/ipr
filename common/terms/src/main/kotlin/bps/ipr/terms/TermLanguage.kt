package bps.ipr.terms

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
     * maps normalized functor symbols to their arities.
     */
    val arityInternTable: Map<String, Int> get() = emptyMap()

    /**
     * Clear the state of the language.  For example, if the language allows a functor symbol to have any arity but not
     * multiple arities, this might be used to reset the arities of functor symbols.
     *
     * The default implementation does nothing since the default implementation allows everything.
     */
    fun clear() {
    }

    /**
     * Ensures that the [symbol] meets any requirements of the language.  E.g., some languages may
     * restrict the names of variables.
     *
     * The default implementation simply returns the receiver if non-blank.
     * @return [symbol] or `null`.
     * @param symbol the [String] to be checked
     */
    fun registerFreeVariableOrNull(symbol: String): String? =
        symbol.takeIf { it.isNotBlank() }

//    /**
//     * The default implementation simply returns the receiver.
//     * @return the normalized variable symbol derived from the input string, if any.
//     */
//    fun toNormalizedBoundVariableOrNull(symbol: String): String? =
//        symbol.takeIf { it.isNotBlank() }

    /**
     * Ensures that the [symbol] meets any requirements of the language.  E.g., some languages may
     * 1. restrict the names of functors or
     * 2. require functors to have a single arity.
     * The default implementation simply returns the receiver if non-blank.
     * @return [symbol] or `null`.
     * @param arity the intended arity of the functor symbol
     * @param symbol the [String] to be checked
     */
    fun registerFunctorOrNull(symbol: String, arity: Int): String? =
        symbol.takeIf { it.isNotBlank() }

    /**
     * Allows absolutely everything except pure whitespace.
     */
    companion object : TermLanguage

}

/**
 * Allows any non-empty [String] symbol to be a variable or a functor.  A given symbol can be used as
 * both a variable and a functor in a single instance of this [TermLanguage].  However, a given functor symbol has a
 * fixed arity once it is registered.
 */
open class FolTermLanguage : TermLanguage {

    protected val internalArityInternTable = mutableMapOf<String, Int>()
    override val arityInternTable: Map<String, Int>
        get() = internalArityInternTable

    override fun registerFunctorOrNull(symbol: String, arity: Int): String? =
        super
            .registerFunctorOrNull(symbol, arity)
            .takeIf {
                internalArityInternTable
                    .getOrPut(symbol) { arity } == arity
            }

    override fun clear() {
        internalArityInternTable.clear()
    }

}
