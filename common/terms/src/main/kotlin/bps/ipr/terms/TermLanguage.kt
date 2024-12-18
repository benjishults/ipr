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

    val arityInternTable: Map<String, Int> get() = emptyMap()

    /**
     * Clear the state of the language.  For example, if the language allows a functor symbol to have any arity but not
     * multiple arities, this might be used to reset the arities of functor symbols.
     *
     * The default implementation does nothing since the default implementation allows everything.
     */
    fun clear() {}

    /**
     * The default implementation simply returns the receiver.
     * @return the normalized variable symbol derived from the input string if any.
     */
    fun toNormalizedVariableOrNull(symbol: String): String? = symbol

    /**
     * The default implementation simply returns the receiver.
     * @return the normalized functor symbol of the given arity derived from the input string if any.
     */
    fun toNormalizedFunctorOrNull(symbol: String, arity: Int): String? = symbol

    /**
     * Allows absolutely everything.
     */
    companion object : TermLanguage

}

/**
 * Allows any [String] symbol to be a variable and any symbol to be a functor symbol.  A given symbol can be used as
 * both a variable and a functor in a single instance of this [TermLanguage].  However, a given functor symbol has a
 * fixed arity once it is normalized.
 */
object FolTermLanguage : TermLanguage {

    private val internalArityInternTable = mutableMapOf<String, Int>()
    override val arityInternTable: Map<String, Int> = internalArityInternTable

    override fun toNormalizedFunctorOrNull(symbol: String, arity: Int): String? =
        symbol.takeIf { internalArityInternTable.getOrPut(symbol) { arity } == arity }

    override fun clear() {
        internalArityInternTable.clear()
    }

}
