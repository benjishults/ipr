package bps.ipr.terms

interface Unifier {
    /**
     * A [Unifier] needs a [TermImplementation] in order to construct new terms.
     *
     * Generally, a [Unifier] assumes that the [Term]s it is unifying were constructed by the given [TermImplementation]
     * or compatible.
     */
    val termImplementation: TermImplementation
    fun unify(term1: Term, term2: Term, under: IdempotentSubstitution = EmptySubstitution): IdempotentSubstitution?
}

/**
 * This unifier is appropriate for an implementation like [TermImplementation] or [FolTermImplementation] where proper
 * functions are not interned.  This does assume that free variables and constants with the same symbol are equal.
 */
data class GeneralRecursiveDescentUnifier(
    override val termImplementation: TermImplementation = FolTermImplementation(),
) : Unifier {
    // FIXME make a version of this that can assume that under has already been applied
    override fun unify(term1: Term, term2: Term, under: IdempotentSubstitution): IdempotentSubstitution? =
        // this short-circuit will be particularly useful when terms are interned
        if (term1 === term2)
            under
        else
            when (term1) {
                is FreeVariable ->
                    under
                        .map(term1)
                        // TODO can I get rid of this bit if I ensure under has been applied ahead of time?
                        .takeIf { it != term1 }
                        ?.let {
                            unify(it, term2, under)
                        }
                        ?: when (term2) {
                            is FreeVariable ->
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
                                            FreeVariable.makeSubstitution(term1, term2),
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
                        is FreeVariable ->
                            unify(term2, term1, under)
                        is Constant ->
                            null
                        is ProperFunction ->
                            if (term1.symbol == term2.symbol) {
                                term1.arguments.foldIndexed(under) { index, runningSub, term ->
                                    unify(
                                        term
                                            .apply(runningSub, termImplementation),
                                        term2.arguments[index]
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
