package bps.ipr.formulas

import bps.ipr.terms.EmptySubstitution
import bps.ipr.terms.FolTermImplementation
import bps.ipr.terms.GeneralRecursiveDescentTermUnifier
import bps.ipr.terms.IdempotentSubstitution
import bps.ipr.terms.TermImplementation
import bps.ipr.terms.TermUnifier

interface FormulaUnifier {

    val termUnifier: TermUnifier

    /**
     * This allows you to do screwy things you shouldn't do such as
     * passing in a substitution whose domain or variable-range includes variables bound in the given formulas
     */
    fun unify(
        formula1: Predicate,
        formula2: Predicate,
        under: IdempotentSubstitution = EmptySubstitution,
    ): IdempotentSubstitution?

}

/**
 * This unifier is appropriate for an implementation like [TermImplementation] or [FolTermImplementation] where proper
 * functions are not interned.  This does assume that free variables and constants with the same symbol are equal.
 */
class GeneralRecursiveDescentFormulaUnifier(
    termImplementation: TermImplementation = FolTermImplementation(),
) : FormulaUnifier {

    override val termUnifier: TermUnifier = GeneralRecursiveDescentTermUnifier(termImplementation)

    override fun unify(
        formula1: Predicate,
        formula2: Predicate,
        under: IdempotentSubstitution,
    ): IdempotentSubstitution? =
        // this short-circuit will be particularly useful when predicates are interned
        if (formula1 === formula2)
            under
        else if (formula1.symbol == formula2.symbol) {
            formula1
                .arguments
                .foldIndexed(under) { index: Int, runningSub: IdempotentSubstitution, term ->
                    with(termUnifier) {
                        unify(
                            term
                                .apply(runningSub, termImplementation),
                            formula2.arguments[index]
                                .apply(runningSub, termImplementation),
                            runningSub,
                        )
                        // NOTE non-local exit needed to short-circuit when unification fails on an argument
                            ?: return null
                    }
                }
        } else
            null

}
