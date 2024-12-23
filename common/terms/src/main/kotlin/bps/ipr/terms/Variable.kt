package bps.ipr.terms

import kotlin.math.sign

sealed interface Variable : Term/*, Comparable<Variable>*/ {

    val symbol: String

    fun occursFreeIn(term: Term): Boolean =
        this in term.variablesFreeIn

    override fun apply(substitution: Substitution, termImplementation: TermImplementation): Term =
        substitution.map(this)

    override fun display(): String =
        symbol

    companion object {

        // FIXME this should probably be in the [TermLanguage] or the [TermImplementation]
        fun makeSubstitution(var1: Variable, var2: Variable): IdempotentSubstitution =
            when (var1) {
                is FreeVariable ->
                    when (var2) {
                        is FreeVariable ->
                            when (var1.compareTo(var2).sign) {
                                0 ->
                                    EmptySubstitution
                                -1 ->
                                    SingletonIdempotentSubstitution(var2, var1)
                                else ->
                                    SingletonIdempotentSubstitution(var1, var2)
                            }
                        else ->
                            SingletonIdempotentSubstitution(var1, var2)
                    }
//                is BoundVariable ->
//                    when (var2) {
//                        is BoundVariable ->
//                            when (var1.compareTo(var2).sign) {
//                                0 ->
//                                    EmptySubstitution
//                                -1 ->
//                                    SingletonIdempotentSubstitution(var2, var1)
//                                else ->
//                                    SingletonIdempotentSubstitution(var1, var2)
//                            }
//                        else ->
//                            SingletonIdempotentSubstitution(var2, var1)
//                    }
            }

    }

}

class FreeVariable(
    override val symbol: String,
) : Variable, Comparable<FreeVariable> {

    override val variablesFreeIn: Set<Variable> = setOf(this)

    override fun compareTo(other: FreeVariable): Int =
        symbol.compareTo(other.symbol)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FreeVariable) return false

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

//// TODO gotta thing about what to do about interning, here.
//class BoundVariable(
//    override val symbol: String,
//    /**
//     * Caller is responsible to ensure any desired uniqueness constraints.  If not specified, this will be a random
//     * [Long].
//     */
//    val id: Long = Random.nextLong(),
//) : Variable, Comparable<BoundVariable> {
//
//    override val variablesFreeIn: Set<Variable> = setOf(this)
//
//    override fun apply(substitution: Substitution, termImplementation: TermImplementation): Term =
//        substitution.map(this)
//
//    override fun compareTo(other: BoundVariable): Int =
//        symbol
//            .compareTo(other.symbol)
//            .sign
//            .let {
//                when (it) {
//                    0 -> id.compareTo(other.id)
//                    else -> it
//                }
//            }
//
//    override fun toString(): String =
//        display()
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is BoundVariable) return false
//
//        if (id != other.id) return false
//        if (symbol != other.symbol) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = id.hashCode()
//        result = 31 * result + symbol.hashCode()
//        return result
//    }
//
//}
