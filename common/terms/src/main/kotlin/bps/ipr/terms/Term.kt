package bps.ipr.terms

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sign

sealed interface Term {

    /**
     * Variables that occur free in this term.
     */
    val freeVariables: Set<Variable>

    fun unify(term: Term): Substitution?

    fun apply(substitution: Substitution): Term

    fun display(): String

}

sealed interface Variable : Term {

    val symbol: VariableSymbol

    fun occursFreeIn(term: Term): Boolean =
        this in term.freeVariables

    data class VariableSymbol(
        val displaySymbol: String,
        val order: Long,
    ) : Comparable<VariableSymbol> {
        init {
            require(displaySymbol.isNotEmpty())
        }

        override fun compareTo(other: VariableSymbol): Int =
            order.compareTo(other.order)
    }

}

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
data class FreeVariable private constructor(
    override val symbol: Variable.VariableSymbol,
) : Variable, Comparable<FreeVariable> {
    override val freeVariables: Set<FreeVariable> = setOf(this)

    override fun unify(term: Term): Substitution? =
        when (term) {
            is ProperFunction -> {
                if (this.occursFreeIn(term))
                    null
                else
                    SingletonSubstitution(this, term)
            }
            is Constant -> SingletonSubstitution(this, term)
//            is BoundVariable -> null
            is FreeVariable -> if (this == term) EmptySubstitution else makeSubstitution(this, term)
//            else -> null
        }

    override fun apply(substitution: Substitution): Term =
        substitution.map(this)

    override fun display(): String =
        symbol.displaySymbol

    override fun compareTo(other: FreeVariable): Int =
        symbol.compareTo(other.symbol)

    companion object : Comparator<FreeVariable> {
        //        private object VariableSymbolTable : Comparator<FreeVariable> {
//        @Volatile
//        private var count: Long = 0L
//        private val variables = ConcurrentHashMap<String, Variable>()
        private val namesToCounts = ConcurrentHashMap<String, Long>()

        fun makeSubstitution(var1: FreeVariable, var2: FreeVariable): Substitution =
            when (var1.compareTo(var2).sign) {
                0 -> EmptySubstitution
                -1 -> SingletonSubstitution(var2, var1)
                else -> SingletonSubstitution(var1, var2)
            }

        override fun compare(o1: FreeVariable, o2: FreeVariable): Int =
            o1.symbol.compareTo(o2.symbol)

        //    fun intern(displaySymbol: String): Variable {
//        return variables.getOrPut(displaySymbol) {
//            Variable(displaySymbol)
//        }
//    }
        operator fun invoke(displaySymbol: String) =
            FreeVariable(
                Variable.VariableSymbol(
                    displaySymbol,
                    namesToCounts.compute(displaySymbol) { _, count: Long? ->
                        1L
                            .takeIf { count === null }
                            ?: (count!! + 1)
                    }!!,
                ),
            )

    }
}

data class FunctionSymbol(
    val displaySymbol: String,
)

sealed class Function(
    val symbol: FunctionSymbol,
    val arity: Int,
    val arguments: List<Term>,
) : Term {
    init {
        require(arguments.size == arity)
    }

    override val freeVariables: Set<Variable> =
        arguments
            .flatMap { it.freeVariables }
            .toSet()

    override fun display(): String {
        return "${symbol.displaySymbol}${
            arguments
                .map { it.display() }
                .joinToString(", ", "(", ")") { it }
        }"
    }

    override fun toString(): String =
        display()

}

// TODO consider making specialty classes up to a certain arity to avoid using a list
//     in many cases.  Might be interesting to measure performance differences.
class ProperFunction(
    symbol: FunctionSymbol,
    arity: Int,
    arguments: List<Term>,
) : Function(symbol, arity, arguments) {
    init {
        require(arity > 0)
    }

    override fun unify(term: Term): Substitution? {
        TODO("Not yet implemented")
    }

    override fun apply(substitution: Substitution): Term {
        TODO("Not yet implemented")
    }

}

//@ConsistentCopyVisibility
class Constant private constructor(
    symbol: FunctionSymbol,
) : Function(symbol, 0, emptyList()) {

    override fun display(): String =
        symbol.displaySymbol

    companion object {
        operator fun invoke(symbol: String) =
            Constant(FunctionSymbol(symbol))
    }

    override fun unify(term: Term): Substitution? {
        TODO("Not yet implemented")
    }

    override fun apply(substitution: Substitution): Term {
        TODO("Not yet implemented")
    }
}

