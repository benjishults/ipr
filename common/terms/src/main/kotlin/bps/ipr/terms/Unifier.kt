package bps.ipr.terms

interface Unifier {
    fun unify(term1: Term, term2: Term): Substitution?
}

class DagUnifier : Unifier {
    override fun unify(term1: Term, term2: Term): Substitution? {
        TODO(
            """
        // TODO construct the union-find-compatible structure from the two terms
        // TODO unify those
        // TODO extract the substitution
        """.trimIndent(),
        )
    }

}
