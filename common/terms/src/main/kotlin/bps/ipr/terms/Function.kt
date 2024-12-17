package bps.ipr.terms

sealed class Function(
    val symbol: String,
    val arity: Int,
) : Term {

    override fun toString(): String =
        display()

}

class Constant(
    symbol: String,
) : Function(symbol, 0) {

    override val freeVariables: Set<Variable> = emptySet()

    override fun display(): String =
        "$symbol()"

    override fun unifyOrNull(term: Term, under: Substitution): Substitution? =
        when (term) {
            is FreeVariable -> term.unifyOrNull(this, under)
            is Constant ->
                if (this == term)
                    under
                else
                    null
            is ProperFunction -> null
        }

    override fun apply(substitution: Substitution): Term {
        TODO("Not yet implemented")
    }

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

    override val freeVariables: Set<Variable> =
        arguments
            .flatMap { it.freeVariables }
            .toSet()

    override fun display(): String {
        return "$symbol${
            arguments
                .map { it.display() }
                .joinToString(", ", "(", ")") { it }
        }"
    }

    // FIXME either get under working properly or get rid of it
    override fun unifyOrNull(term: Term, under: Substitution): Substitution? =
        when (term) {
            is ProperFunction ->
                if (term.symbol == symbol) {
                    if (term.arguments == arguments)
                        under
                    else
                        arguments.foldIndexed(under) { index: Int, runningSubstitution: Substitution?, arg: Term ->
                            runningSubstitution
                                ?.let {
                                    arg.unifyOrNull(term.arguments[index], runningSubstitution)
                                        ?.let { runningSubstitution.combine(it) }
                                }
                        }
                } else
                    null
            is Constant -> null
            is FreeVariable -> term.unifyOrNull(this, under)
        }

    override fun apply(substitution: Substitution): Term {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProperFunction

        return symbol == other.symbol &&
                arity == other.arity &&
                arguments == other.arguments
    }

    override fun hashCode(): Int {
        return arguments.hashCode()
    }

}
