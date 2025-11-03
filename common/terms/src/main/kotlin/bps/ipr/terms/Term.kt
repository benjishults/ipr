package bps.ipr.terms

sealed interface Term {

    /**
     * Variables that occur free in this term.
     */
    val variablesFreeIn: Set<Variable>

    // TODO consider moving this to a subtype SyntacticTerm
    fun apply(substitution: Substitution, termImplementation: TermImplementation): Term

    fun display(): String

}
