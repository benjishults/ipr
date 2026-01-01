package bps.ipr.terms

import bps.ipr.substitution.Substitution

@JvmInline
value class Functor(val symbol: String) {
    init {
        require(symbol.isNotEmpty())
    }

    override fun toString(): String = symbol
}

sealed class Function(
    val functor: Functor,
    val arity: Int,
) : Term {

    override fun toString(): String =
        display()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false

        if (arity != other.arity) return false
        if (functor != other.functor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arity
        result = 31 * result + functor.hashCode()
        return result
    }

}

class Constant(
    functor: Functor,
) : Function(functor, 0) {

    override val variablesFreeIn: Set<Variable> =
        emptySet()

    override fun apply(substitution: Substitution, termImplementation: TermImplementation): Term =
        this

    override fun display(): String =
        "$functor()"

}

// TODO consider making specialty classes up to a certain arity to avoid using a list
//     in many cases.  Might be interesting to measure performance differences.  UnaryFunction, BinaryFunction, Arity5Function.
class ProperFunction(
    functor: Functor,
    /**
     * For performance, this argument is not protected from mutilation by the caller.  We're assuming the caller is not
     * trying to break us.
     */
    val arguments: ArgumentList,
) : Function(functor, arguments.count()) {

    init {
        require(
            arguments.firstOrNull() !== null ||
                    false.also {
                        println("arguments $arguments")
                    },
        )
    }

    override val variablesFreeIn: Set<Variable> =
        arguments
            .flatMapTo(mutableSetOf()) { it.variablesFreeIn }

    override fun apply(substitution: Substitution, termImplementation: TermImplementation): Term =
        // short-circuit if we know the substitution won't disturb this term
        if (substitution
                .domain
                .firstOrNull { it in this.variablesFreeIn }
            !== null
        )
            termImplementation.properFunction(
                functor,
                arguments
                    .map {
                        it.apply(substitution, termImplementation)
                    },
            )!!
        else
            this

    override fun display(): String =
        arguments
            .map { it.display() }
            .joinToString(", ", "$functor(", ")") { it }

    override fun equals(other: Any?): Boolean =
        super.equals(other) &&
                other is ProperFunction &&
                arguments == other.arguments

    override fun hashCode(): Int =
        31 * super.hashCode() + arguments.hashCode()

}
