package bps.ipr.common

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.ExperimentalExtendedContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class, ExperimentalExtendedContracts::class)
fun <T> Set<T>?.combineNullable(
    rest: Set<T>?,
): Set<T>? {
    contract {
        (this@combineNullable !== null) implies returnsNotNull()
        (rest !== null) implies returnsNotNull()
    }
    return this
        .let { receiver ->
            rest
                ?.let { rest ->
                    if (receiver === null)
                        rest
                    else
                        rest.plus(receiver)
                }
                ?: receiver
        }
}

@OptIn(ExperimentalContracts::class, ExperimentalExtendedContracts::class)
fun <T: Any> IdentitySet<T>?.combineNullable(
    rest: IdentitySet<T>?,
): IdentitySet<T>? {
    contract {
        (this@combineNullable !== null) implies returnsNotNull()
        (rest !== null) implies returnsNotNull()
    }
    return this
        .let { receiver ->
            rest
                ?.let { rest ->
                    if (receiver === null)
                        rest
                    else
                        rest.plus(receiver)
                }
                ?: receiver
        }
}
