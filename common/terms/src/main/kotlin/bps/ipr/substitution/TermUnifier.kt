package bps.ipr.substitution

import bps.ipr.terms.Constant
import bps.ipr.terms.FolTermImplementation
import bps.ipr.terms.FreeVariable
import bps.ipr.terms.ProperFunction
import bps.ipr.terms.Term
import bps.ipr.terms.TermImplementation
import bps.ipr.terms.Variable

interface TermUnifier {
    /**
     * A [TermUnifier] needs a [bps.ipr.terms.TermImplementation] in order to construct new terms.
     *
     * Generally, a [TermUnifier] assumes that the [bps.ipr.terms.Term]s it is unifying were constructed by the given [bps.ipr.terms.TermImplementation]
     * or compatible.
     */
    val termImplementation: TermImplementation
    fun unify(term1: Term, term2: Term, under: IdempotentSubstitution = EmptySubstitution): IdempotentSubstitution?
}

/**
 * This unifier is appropriate for an implementation like [TermImplementation] or [bps.ipr.terms.FolTermImplementation] where proper
 * functions are not interned.  This does assume that free variables and constants with the same symbol are equal.
 */
data class GeneralRecursiveDescentTermUnifier(
    override val termImplementation: TermImplementation = FolTermImplementation(),
) : TermUnifier {

    override fun unify(term1: Term, term2: Term, under: IdempotentSubstitution): IdempotentSubstitution? =
        // this short-circuit will be particularly useful when terms are interned
        if (term1 === term2)
            under
        else
            when (term1) {
                is Variable ->
                    under
                        .map(term1)
                        .takeIf { it != term1 }
                        ?.let {
                            unify(it, term2, under)
                        }
                        ?: when (term2) {
                            is Variable ->
                                under
                                    .map(term2)
                                    .takeIf { it != term2 }
                                    ?.let {
                                        unify(term1, it, under)
                                    }
                                    ?: if (term1 == term2) {
                                        under
                                    } else
                                        under.composeIdempotent(
                                            Variable.Companion.makeSubstitution(term1, term2),
                                            termImplementation,
                                        )
                            else ->
                                if (term1.occursFreeIn(term2)) {
                                    null
                                } else
                                    under.composeIdempotent(
                                        SingletonIdempotentSubstitution(term1, term2),
                                        termImplementation,
                                    )
                        }
                is Constant ->
                    if (term1 == term2) {
                        under
                    } else
                        when (term2) {
                            // orient
                            is FreeVariable ->
                                unify(term2, term1, under)
                            else ->
                                null
                        }
                is ProperFunction ->
                    when (term2) {
                        // orient
                        is Variable ->
                            unify(term2, term1, under)
                        is Constant ->
                            null
                        is ProperFunction ->
                            if (term1.functor == term2.functor) {
                                term1.arguments.foldIndexed(under) { index, runningSub, term ->
                                    unify(
                                        term
                                            .apply(runningSub, termImplementation),
                                        term2.arguments.elementAt(index)
                                            .apply(runningSub, termImplementation),
                                        runningSub,
                                    )
                                    // NOTE non-local exit
                                        ?: return null
                                }
                            } else
                                null
                    }
            }

}

//class DagUnifier : Unifier {
//    override fun unify(term1: Term, term2: Term, under: Substitution): Substitution? {
//        TODO(
//            """
//        // TODO construct the union-find-compatible structure from the two terms if that's not what we're looking at
//        // TODO unify those
//        // TODO extract the substitution
//        """.trimIndent(),
//        )
//    }
//
//}
