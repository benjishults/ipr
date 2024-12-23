package bps.ipr.formulas

import bps.ipr.terms.FolTermLanguage
import bps.ipr.terms.TermLanguage

/**
 * A [FormulaLanguage] controls
 * 1. what symbol is allowed to be a predicate
 * 2. the allowed arity of a given predicate symbol
 * 3. the symbolic representation of the standard formula builders of the language (e.g. "and" and "or")
 *
 * A term language can be very liberal, allowing anything to be anything and returning new structure on every call.
 * Or it can be very controlling of arity and conserve structure as much as possible.
 */
interface FormulaLanguage {

//    val arityInternTable: Map<String, Int> get() = emptyMap()

    /**
     * Clear the state of the language.  For example, if the language allows a predicate symbol to have any arity but not
     * multiple arities, this might be used to reset the arities of predicate symbols.
     *
     * The default implementation does nothing since the default implementation allows everything.
     */
    fun clear() {}

    /**
     * The default implementation simply returns the receiver.
     * @return the normalized predicate symbol of the given arity derived from the input string if any.
     * @param arity defaults to `0`
     */
    fun toNormalizedPredicateOrNull(symbol: String, arity: Int = 0): String? = symbol

    /**
     * Allows absolutely everything.
     */
    companion object : FormulaLanguage

}

/**
 * Allows any [String] symbol to be a predicate symbol.  However, a given predicate symbol has a
 * fixed arity once it is normalized.
 */
open class FolFormulaLanguage(
//    val folTermLanguage: FolTermLanguage = FolTermLanguage(),
) : FormulaLanguage {

    protected val arityInternTable: MutableMap<String, Int> = mutableMapOf()

    override fun toNormalizedPredicateOrNull(symbol: String, arity: Int): String? =
        symbol.takeIf {
            arityInternTable
                .getOrPut(symbol) { arity } == arity
        }

    override fun clear() {
        arityInternTable.clear()
    }

}
