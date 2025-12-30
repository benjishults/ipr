package bps.ipr.terms

import bps.ipr.substitution.Substitution

sealed class Function(
    val symbol: String,
    val arity: Int,
) : Term {

    override fun toString(): String =
        display()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false

        if (arity != other.arity) return false
        if (symbol != other.symbol) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arity
        result = 31 * result + symbol.hashCode()
        return result
    }

}

class Constant(
    symbol: String,
) : Function(symbol, 0) {

    override val variablesFreeIn: Set<Variable> =
        emptySet()

    override fun apply(substitution: Substitution, termImplementation: TermImplementation): Term =
        this

    override fun display(): String =
        "$symbol()"

}

// TODO consider making specialty classes up to a certain arity to avoid using a list
//     in many cases.  Might be interesting to measure performance differences.  UnaryFunction, BinaryFunction, Arity5Function.
class ProperFunction(
    symbol: String,
    /**
     * For performance, this argument is not protected from mutilation by the caller.  We're assuming the caller is not
     * trying to break us.
     */
    val arguments: ArgumentList,
) : Function(symbol, arguments.size) {

    init {
        require(arguments.isNotEmpty())
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
                symbol,
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
            .joinToString(", ", "$symbol(", ")") { it }

    override fun equals(other: Any?): Boolean =
        super.equals(other) &&
                other is ProperFunction &&
                arguments == other.arguments

    override fun hashCode(): Int =
        31 * super.hashCode() + arguments.hashCode()

}
