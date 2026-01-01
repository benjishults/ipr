package bps.ipr.formulas

import bps.ipr.terms.ArgumentList
import bps.ipr.terms.ArityOverloadException
import bps.ipr.terms.FolTermImplementation
import bps.ipr.terms.Term
import bps.ipr.terms.Variable

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

// FIXME we need a DAG implementation of this as well.

open class FolFormulaImplementation(
    override val formulaLanguage: FolFormulaLanguage = FolFormulaLanguage(),
    val termImplementation: FolTermImplementation = FolTermImplementation(),
) : FormulaImplementation {

    override fun clear() {
        super.clear()
        termImplementation.clear()
    }

    fun predicate(symbol: String, arguments: List<Term> = emptyList()): Predicate =
        formulaLanguage
            .ensurePredicateOrNull(symbol, arguments.size)
            ?.let {
                Predicate(it, ArgumentList(arguments))
            }
            ?: throw ArityOverloadException(
                "$symbol is being used with arity ${arguments.size} but is already defined with arity ${
                    formulaLanguage.getPredicateArity(symbol)
                }",
            )

    open fun truthOrNull(): Truth = Truth

    open fun falsityOrNull(): Falsity = Falsity

    open fun notOrNull(subFormula: FolFormula): Not =
        Not(subFormula)

    open fun andOrNull(subFormulas: List<FolFormula>): And =
        And(subFormulas)

    open fun orOrNull(subFormulas: List<FolFormula>): Or =
        Or(subFormulas)

    open fun iffOrNull(subFormulas: List<FolFormula>): Iff =
        Iff(subFormulas)

    open fun impliesOrNull(subFormulas: List<FolFormula>): Implies =
        Implies(subFormulas)

    open fun forAllOrNull(boundVariables: List<Variable>, subFormula: FolFormula): ForAll =
        ForAll(boundVariables, subFormula)

    open fun forSomeOrNull(boundVariables: List<Variable>, subFormula: FolFormula): ForSome =
        ForSome(boundVariables, subFormula)

}
