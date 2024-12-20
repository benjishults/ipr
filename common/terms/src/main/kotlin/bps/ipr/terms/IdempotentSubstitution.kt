package bps.ipr.terms

/**
 * Mapping from [Variable]s to [Term]s with absolutely no other restrictions.  It is always assumed that a
 * [Substitution] maps a [Variable] to itself unless the contrary is explicitly called out.  In other words, if a
 * [Variable] is not in the domain of a [Substitution], then that [Variable] is mapped to itself.
 */
interface Substitution {

    // NOTE I think there is a mistake in the Baader-Snyder algorithm for composition.  They define domain as
    //   variables that are not mapped to themselves.  However, their algorithm for composition of substitutions
    //   assumes that the definition of domain is more about whether or not the variable is in the syntactic representation
    //   of the substitution.
    val domain: Set<Variable>

    // FIXME remove this if it is unneeded after algorithms are tested
    val range: List<Term>

    // FIXME remove this if it is unneeded after algorithms are tested
//    val varRange: Set<Variable>

    fun isIdempotent(): Boolean =
        domain.all { variable: Variable ->
            range.all { term: Term ->
                !variable.occursFreeIn(term)
            }
        }
//    domain.intersect(varRange).isEmpty()
//        domain.all { variable -> variable !in varRange }

    /**
     * @return [variable] if it isn't mapped by the receiver or the [Term] it is mapped to if it is in the [domain].
     */
    fun map(variable: Variable): Term

    fun display(): String

    /**
     * @return the sub-substitution of the receiver that excludes the [Variable]s in [vars] from the [domain].
     */
    fun withoutBindingsFor(vars: Set<Variable>): Substitution

}

sealed interface IdempotentSubstitution : Substitution {

    override fun withoutBindingsFor(vars: Set<Variable>): IdempotentSubstitution

    // TODO consider moving this to a subtype something like SyntacticSubstitution
    // NOTE we can't cheaply guarantee the result will be idempotent without assuming the inputs are idempotent
    fun composeIdempotent(theta: IdempotentSubstitution, termImplementation: TermImplementation): IdempotentSubstitution

}

data object EmptySubstitution : IdempotentSubstitution {
    override val domain: Set<Variable> = emptySet()
    override val range: List<Term> = emptyList()
//    override val varRange: Set<Variable> = emptySet()

    override fun map(variable: Variable): Term =
        variable

    override fun display(): String =
        "{}"

    override fun withoutBindingsFor(vars: Set<Variable>): IdempotentSubstitution =
        this

    override fun composeIdempotent(
        theta: IdempotentSubstitution,
        termImplementation: TermImplementation,
    ): IdempotentSubstitution =
        theta

    override fun toString(): String =
        display()

}

sealed interface NonEmptyIdempotentSubstitution : IdempotentSubstitution {

    /**
     * @return the result of applying [substitution] to each element of the range of the receiver.
     */
    // TODO is the resulting substitution really guaranteed to be idempotent?  I don't think so.  But maybe it is
    //   given the way this function is used in my algorithms.  Figure this out and document what is expected from
    //   the arguments.
    fun applyToRange(
        substitution: IdempotentSubstitution,
        termImplementation: TermImplementation,
    ): IdempotentSubstitution

    /**
     * @throw IllegalArgumentException if [theta]'s variable range intersects the receiver's domain
     */
    override fun composeIdempotent(
        theta: IdempotentSubstitution,
        termImplementation: TermImplementation,
    ): IdempotentSubstitution {
        // apply other to every term in the range of sigma: sigma_1
        val sigma1: IdempotentSubstitution = applyToRange(
            theta,
            termImplementation,
        )
        require(sigma1.isIdempotent())
        // remove from other any binding of a variable that occurs in the domain of sigma: theta_1
        val theta1: IdempotentSubstitution = theta.withoutBindingsFor(this.domain)
        // take the union of sigma_2 and theta_1
        return when (sigma1) {
            is EmptySubstitution ->
                theta1
            is SingletonIdempotentSubstitution ->
                when (theta1) {
                    is EmptySubstitution ->
                        sigma1
                    is SingletonIdempotentSubstitution ->
                        MultiIdempotentSubstitution(
                            mapOf(
                                sigma1.key to sigma1.value,
                                theta1.key to theta1.value,
                            ),
                        )
                    is MultiIdempotentSubstitution ->
                        theta1.union(sigma1)
                }
            is MultiIdempotentSubstitution ->
                when (theta1) {
                    is NonEmptyIdempotentSubstitution ->
                        sigma1.union(theta1) // trouble is, this result is not idempotent
                    else ->
                        sigma1
                }
        }
    }

}

// TODO consider making specialty classes up to a certain number of variables in a substitution to avoid using a map
//     in many cases.  Might be interesting to measure performance differences.
data class SingletonIdempotentSubstitution(
    val key: Variable,
    val value: Term,
) : NonEmptyIdempotentSubstitution {
    init {
        // NOTE delete this once the factories have been validated... if there are factories... and there should be
        require(!key.occursFreeIn(value))
    }

    override val domain: Set<Variable> = setOf(key)
    override val range: List<Term> = listOf(value)
//    override val varRange: Set<Variable> = value.freeVariables

    override fun withoutBindingsFor(vars: Set<Variable>): IdempotentSubstitution =
        if (key in vars)
            EmptySubstitution
        else
            this

    override fun map(variable: Variable): Term =
        value
            .takeIf { variable == key }
            ?: variable

    override fun display(): String =
        "{$key ↦ ${value.display()}}" // \u21a6

    override fun applyToRange(
        substitution: IdempotentSubstitution,
        termImplementation: TermImplementation,
    ): IdempotentSubstitution =
        value
            .apply(substitution, termImplementation)
            .takeIf { it != this.key }
            ?.let { SingletonIdempotentSubstitution(key, value.apply(substitution, termImplementation)) }
            ?: EmptySubstitution

    override fun toString(): String =
        display()

}

class MultiIdempotentSubstitution(
    mapping: Map<Variable, Term>,
) : NonEmptyIdempotentSubstitution {

    /**
     * Be very careful using this.  This DOES NOT COMPOSE.  This really just takes the union.
     * If there are duplicate keys between the receiver and [other], then [other] will win.
     */
    fun union(other: NonEmptyIdempotentSubstitution): MultiIdempotentSubstitution =
        when (other) {
            is SingletonIdempotentSubstitution ->
                MultiIdempotentSubstitution(mapping + (other.key to other.value))
            is MultiIdempotentSubstitution ->
                MultiIdempotentSubstitution(mapping + other.mapping)
        }

    private val mapping: Map<Variable, Term> =
        mapping.toMap()
    override val domain: Set<Variable> =
        mapping.keys
    override val range: List<Term> =
        mapping
            .values
            .toList()
//    override val varRange: Set<Variable> =
//        range
//            .flatMap { it.freeVariables }
//            .toSet()

    init {
        require(mapping.size > 1)
        // TODO remove this and document that it's missing after factories are validated
        require(isIdempotent())
    }

    override fun map(variable: Variable): Term =
        mapping[variable]
            ?: variable

    override fun display(): String =
        buildString {
            append("{")
            append(
                mapping
                    .map { (key, value) ->
                        // \u21a6
                        "$key ↦ ${value.display()}"
                    }
                    .joinToString(", "),
            )
            append("}")
        }

    override fun withoutBindingsFor(vars: Set<Variable>): IdempotentSubstitution =
        mapping
            .keys
            .minus(vars)
            .let { setDifference ->
                when (setDifference.size) {
                    mapping.size -> // skip building the new map if it will end up being the same thing
                        this
                    else ->
                        setDifference // create a new mapping just for the set difference
                            .associateWith { key ->
                                mapping[key]!!
                            }
                            .let { newMapping ->
                                when (newMapping.size) {
                                    1 ->
                                        newMapping
                                            .entries
                                            .first()
                                            .let { (key, value) ->
                                                SingletonIdempotentSubstitution(key, value)
                                            }
                                    else ->
                                        MultiIdempotentSubstitution(newMapping)
                                }
                            }
                }
            }

    override fun applyToRange(
        substitution: IdempotentSubstitution,
        termImplementation: TermImplementation,
    ): IdempotentSubstitution = // FIXME skip this if we can tell nothing is going to happen
        MultiIdempotentSubstitution(
            mapping
                .mapValues { (_, value) ->
                    value.apply(substitution, termImplementation)
                }
                .filter { (key, value) -> key != value },
        )

    override fun toString(): String =
        display()

}
