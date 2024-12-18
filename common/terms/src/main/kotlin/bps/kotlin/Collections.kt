package bps.kotlin

import bps.ipr.terms.Term

inline fun <T : Any> Iterable<T>.findIndexedOrNull(predicate: (Int, T) -> Boolean): T? {
    forEachIndexed { i, e -> if (predicate(i, e)) return e }
    return null
}

inline fun <T : Any> Iterable<T>.allIndexed(predicate: (Int, T) -> Boolean): Boolean {
    forEachIndexed { i, t -> if (!predicate(i, t)) return false }
    return true
}
