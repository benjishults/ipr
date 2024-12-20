package bps.ipr.terms

import kotlin.math.sign

sealed interface Variable : Term {

    val symbol: String

    fun occursFreeIn(term: Term): Boolean =
        this in term.freeVariables

}

class FreeVariable(
    override val symbol: String,
) : Variable, Comparable<FreeVariable> {

    override val freeVariables: Set<FreeVariable> = setOf(this)

    override fun apply(substitution: IdempotentSubstitution, termImplementation: TermImplementation): Term =
        substitution.map(this)

    override fun display(): String =
        symbol

    override fun compareTo(other: FreeVariable): Int =
        symbol.compareTo(other.symbol)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FreeVariable

        return symbol == other.symbol
    }

    override fun hashCode(): Int {
        return symbol.hashCode()
    }

    override fun toString(): String = display()

    companion object {

        // FIXME this should probably be in the [TermLanguage] or the [TermImplementation]
        fun makeSubstitution(var1: FreeVariable, var2: FreeVariable): IdempotentSubstitution =
            when (var1.compareTo(var2).sign) {
                0 -> EmptySubstitution
                -1 -> SingletonIdempotentSubstitution(var2, var1)
                else -> SingletonIdempotentSubstitution(var1, var2)
            }

    }

}
