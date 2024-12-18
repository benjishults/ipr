package bps.ipr.terms

import kotlin.math.sign

sealed interface Variable : Term {

    val symbol: String

    fun occursFreeIn(term: Term): Boolean =
        this in term.freeVariables

}

data class FreeVariable(
    override val symbol: String,
) : Variable, Comparable<FreeVariable> {
    override val freeVariables: Set<FreeVariable> = setOf(this)

    // FIXME either get rid of under or use it properly
    override fun unifyOrNull(term: Term, under: Substitution): Substitution? =
        when (term) {
            is ProperFunction ->
                if (this.occursFreeIn(term))
                    null
                else
                    SingletonSubstitution(this, term)
            is Constant -> SingletonSubstitution(this, term)
            is FreeVariable ->
                if (this == term)
                    EmptySubstitution
                else
                    makeSubstitution(this, term)
        }

    override fun apply(substitution: Substitution): Term =
        substitution.map(this)

    override fun display(): String =
        symbol

    override fun compareTo(other: FreeVariable): Int =
        symbol.compareTo(other.symbol)

    companion object {

        private fun makeSubstitution(var1: FreeVariable, var2: FreeVariable): Substitution =
            when (var1.compareTo(var2).sign) {
                0 -> EmptySubstitution
                -1 -> SingletonSubstitution(var2, var1)
                else -> SingletonSubstitution(var1, var2)
            }

    }

}
