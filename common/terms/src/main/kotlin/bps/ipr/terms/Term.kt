package bps.ipr.terms

import kotlin.math.sign

sealed interface Term {

    /**
     * Variables that occur free in this term.
     */
    val freeVariables: Set<Variable>

    fun unifyOrNull(term: Term): Substitution?

    fun apply(substitution: Substitution): Term

    fun display(): String

}

sealed interface Variable : Term {

    val symbol: String

    fun occursFreeIn(term: Term): Boolean =
        this in term.freeVariables

}

// NOTE KISS for now
//@@ConsistentCopyVisibility
//data class BoundVariable private constructor(
//    override val symbol: Variable.VariableSymbol,
////    val internalSymbol: String,
//) : Variable {
//
//    override val freeVariables: Set<FreeVariable> = emptySet()
//
//    override fun unify(term: Term): Substitution? =
//        EmptySubstitution
//            .takeIf {
//                this == term
//            }
//
//    override fun apply(substitution: Substitution): Term =
//        this
//}

@ConsistentCopyVisibility
data class FreeVariable(
    override val symbol: String,
) : Variable, Comparable<FreeVariable> {
    override val freeVariables: Set<FreeVariable> = setOf(this)

    override fun unifyOrNull(term: Term): Substitution? =
        when (term) {
            is ProperFunction -> {
                if (this.occursFreeIn(term))
                    null
                else
                    SingletonSubstitution(this, term)
            }
            is Constant -> SingletonSubstitution(this, term)
            is FreeVariable -> if (this == term) EmptySubstitution else makeSubstitution(this, term)
        }

    override fun apply(substitution: Substitution): Term =
        substitution.map(this)

    override fun display(): String =
        symbol

    override fun compareTo(other: FreeVariable): Int =
        symbol.compareTo(other.symbol)

    companion object {

        fun makeSubstitution(var1: FreeVariable, var2: FreeVariable): Substitution =
            when (var1.compareTo(var2).sign) {
                0 -> EmptySubstitution
                -1 -> SingletonSubstitution(var2, var1)
                else -> SingletonSubstitution(var1, var2)
            }

    }

}

sealed class Function(
    val symbol: String,
    val arity: Int,
) : Term {

    override fun toString(): String =
        display()

}

/**
 * A [List] with equality defined by its members.
 *
 * These are not suitable hash-map keys.
 */
class ArgumentList(arguments: List<Term>) : List<Term> by arguments {

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is ArgumentList &&
                        other.size == this.size &&
                        // kotlin doesn't seem to have an allIndexed function.
                        other.foldIndexed(true) { i, _, t ->
                            if (t != get(i))
                                return false
                            else
                                true
                        })

    override fun hashCode(): Int {
        return javaClass.hashCode()
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

    override fun unifyOrNull(term: Term): Substitution? {
        TODO("Not yet implemented")
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

//@ConsistentCopyVisibility
class Constant(
    symbol: String,
) : Function(symbol, 0) {

    override val freeVariables: Set<Variable> = emptySet()

    override fun display(): String =
        "$symbol()"

    override fun unifyOrNull(term: Term): Substitution? {
        TODO("Not yet implemented")
    }

    override fun apply(substitution: Substitution): Term {
        TODO("Not yet implemented")
    }

}

