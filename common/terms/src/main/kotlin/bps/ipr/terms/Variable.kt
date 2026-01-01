package bps.ipr.terms

import bps.ipr.substitution.EmptySubstitution
import bps.ipr.substitution.IdempotentSubstitution
import bps.ipr.substitution.SingletonIdempotentSubstitution
import bps.ipr.substitution.Substitution
import kotlin.math.sign

sealed interface Variable : Term, Comparable<Variable> {

    val symbol: String

    fun occursFreeIn(term: Term): Boolean =
        this in term.variablesFreeIn

    override fun apply(substitution: Substitution, termImplementation: TermImplementation): Term =
        substitution.map(this)

    override fun display(): String =
        symbol

    override fun compareTo(other: Variable): Int =
        symbol.compareTo(other.symbol)

    companion object {

        // FIXME this should probably be in the [TermLanguage] or the [TermImplementation]
        fun makeSubstitution(var1: Variable, var2: Variable): IdempotentSubstitution =
            when (var1.compareTo(var2).sign) {
                0 ->
                    EmptySubstitution
                -1 ->
                    SingletonIdempotentSubstitution(var2, var1)
                else ->
                    SingletonIdempotentSubstitution(var1, var2)
            }

    }

}

class FreeVariable(
    override val symbol: String,
) : Variable {

    override val variablesFreeIn: Set<Variable> = setOf(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FreeVariable) return false

        return symbol == other.symbol
    }

    override fun hashCode(): Int {
        return symbol.hashCode()
    }

    override fun toString(): String = display()

}

//class BoundVariable(
//    override val symbol: String,
//) : Variable {
//
//    override val variablesFreeIn: Set<Variable> = emptySet()
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is BoundVariable) return false
//
//        return symbol == other.symbol
//    }
//
//    override fun hashCode(): Int {
//        return symbol.hashCode()
//    }
//
//    override fun toString(): String = display()
//
//}
