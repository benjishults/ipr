package bps.ipr.terms

sealed interface Term {

    /**
     * Variables that occur free in this term.
     */
    val freeVariables: Set<Variable>

    fun unifyOrNull(term: Term, under: Substitution = EmptySubstitution): Substitution?

    fun apply(substitution: Substitution): Term

    fun display(): String

}
