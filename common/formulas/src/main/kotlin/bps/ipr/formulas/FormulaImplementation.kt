package bps.ipr.formulas

//import bps.ipr.terms.BoundVariable
import bps.ipr.terms.ArgumentList
import bps.ipr.terms.FolTermImplementation
import bps.ipr.terms.ProperFunction
import bps.ipr.terms.Term

/**
 * A [FormulaImplementation] is a factory for [Formula]s in a given language.  Different implementations might return
 * [Formula]s with different internal structures.  This is useful, for example, when say a structure-conserving DAG
 * representation is useful sometimes but not other times.
 */
interface FormulaImplementation : AutoCloseable {

    /**
     * The [FormulaLanguage] used to determine the normalizations of symbols.
     */
    val formulaLanguage: FormulaLanguage

    /**
     * This implementation is not idempotent.  A [FormulaImplementation] may be meaningfully reused and re-closed any number of
     * times.  However, it is recommended to simply create a new one after one is closed.
     */
    override fun close() =
        clear()

    /**
     * This implementation simply clears the [formulaLanguage]
     */
    fun clear() {
        formulaLanguage.clear()
    }

    /**
     * Allows absolutely everything and interns nothing.
     */
    companion object : FormulaImplementation {
        override val formulaLanguage: FormulaLanguage = FormulaLanguage
    }

}

open class FolFormulaImplementation(
    override val formulaLanguage: FolFormulaLanguage = FolFormulaLanguage(),
    val termImplementation: FolTermImplementation = FolTermImplementation(),
) : FormulaImplementation {

//    protected val formulaConstructorMap: MutableMap<String, (List<FolFormula>, List<BoundVariable>) -> FolFormula?> =
//        mutableMapOf()

    protected val predicateMap: MutableMap<String, Int> = mutableMapOf()

    fun predicateOrNull(symbol: String, arguments: List<Term> = emptyList()): FolFormula? =
        formulaLanguage
            .toNormalizedPredicateOrNull(symbol, arguments.size)
            ?.let {
                Predicate(it, ArgumentList(arguments))
            }

    open fun notOrNull(subFormula: FolFormula): Not =
        Not(subFormula)

    open fun andOrNull(vararg subFormula: FolFormula): And =
        And(*subFormula)

    open fun orOrNull(vararg subFormula: FolFormula): Or =
        Or(*subFormula)

    open fun iffOrNull(vararg subFormula: FolFormula): Equivalence =
        Equivalence(*subFormula)

    open fun impliesOrNull(antecedent: FolFormula, consequent: FolFormula): Implies =
        Implies(antecedent, consequent)

}
