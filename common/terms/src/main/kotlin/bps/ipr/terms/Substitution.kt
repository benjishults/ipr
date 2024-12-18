package bps.ipr.terms

// NOTE the fast algorithms for unification encode the substitution into the term DAG while they're being built.
//   this is not conducive to parallel or breadth-first unification within a tableau, is it?  Unless I copy the dags
//   for each problem.
//   Basically, the very fast algorithms use union-find on the nodes in the term dag.  It might not be too terrible
//   to build this structure for each unification problem we have to do.
// NOTE I think we can get a linear space algorithm without storing the substitution as part of the DAG?
//   or, just use the DAG with the old algorithm for some savings just maybe not asymptotic improvement.
sealed interface Substitution {
    // NOTE do I want to track the v-range for faster combinations/applications?

    /**
     * @return [variable] if it isn't mapped by the receiver or the term it is mapped to if it is mapped.
     */
    // TODO should this return `null` if the var isn't mapped?
    fun map(variable: Variable): Term
//    fun incorporate(pair: SingletonSubstitution): Substitution?

    fun display(): String

    fun combine(other: Substitution): Substitution =
        TODO()

    class Builder {
        private val mapping: MutableMap<Variable, Term> = mutableMapOf()
        fun withPair(variable: Variable, term: Term): Builder =
            this.also {
                mapping[variable] = term
            }

//        fun build(): Substitution {
//            when (mapping.size) {
//                0 -> EmptySubstitution
//                1 -> mapping
//                    .iterator()
//                    .next()
//                    .let { (key, value) ->
//                        SingletonSubstitution(key, value)
//                    }
//            }
//        }
    }
}

data object EmptySubstitution : Substitution {
    override fun map(variable: Variable): Term = variable
    override fun display(): String = "{}"
//    override fun incorporate(pair: SingletonSubstitution): Substitution = pair
}

sealed interface NonEmptySubstitution : Substitution

// TODO consider making specialty classes up to a certain number of variables in a substitution to avoid using a map
//     in many cases.  Might be interesting to measure performance differences.
data class SingletonSubstitution(
    val key: Variable,
    val value: Term,
) : NonEmptySubstitution {
    init {
        // NOTE delete this once the factories have been validated... if there are factories... and there should be
        require(!key.occursFreeIn(value))
    }

    override fun map(variable: Variable): Term =
        value
            .takeIf { variable == key }
            ?: variable

    override fun display(): String =
        "{$key ↦ ${value.display()}}" // \u21a6

//    override fun incorporate(pair: SingletonSubstitution): Substitution? =
//        TODO()

}

/**
 * Though this class may not prevent it, it is an error to pass in a map in which some key occurs in its value.
 */
class MultiSubstitution(
    mapping: Map<FreeVariable, Term>,
) : NonEmptySubstitution {
    init {
        mapping.isNotEmpty()
        // this invariant must be maintained by the caller
        // leave this in place while testing
        mapping.all { (key, value) ->
            !key.occursFreeIn(value)
        }
    }

    private val mapping: Map<FreeVariable, Term> = mapping.toMap()

    override fun map(variable: Variable): Term =
        mapping[variable] ?: variable

    override fun display(): String =
        buildString {
            append("{")
            append(
                mapping
                    .map { (key, value) ->
                        // \u21a6
                        append("$key ↦ ${value.display()}")
                    }
                    .joinToString(", "),
            )
            append("}")
        }

//    override fun incorporate(pair: SingletonSubstitution): Substitution =
//        TODO()
    /*{
        return MultiSubstitution(
            mapping.mapValues { (key, value) ->
                map(key)
            } + pair,
        )
    }*/


}
