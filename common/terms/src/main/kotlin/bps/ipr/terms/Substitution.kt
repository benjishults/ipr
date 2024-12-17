package bps.ipr.terms

sealed interface Substitution {
    /**
     * @return [variable] if it isn't mapped by the receiver or the term it is mapped to if it is mapped.
     */
    // TODO should this return `null` if the var isn't mapped?
    fun map(variable: Variable): Term
//    fun incorporate(pair: SingletonSubstitution): Substitution?

    fun display(): String

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
