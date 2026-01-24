package bps.ipr.common

import java.util.IdentityHashMap

interface IdentitySet<K : Any> : Iterable<K> {
    val size: Int

    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean =
        !isEmpty()

    operator fun contains(element: K): Boolean
    fun containsAll(elements: Collection<K>): Boolean

    override fun iterator(): Iterator<K>

    fun <R : Comparable<R>> maxBy(selector: (K) -> R): K

    fun forEach(action: (K) -> Unit) =
        iterator().forEach(action)

    fun <R : Any> toSet(): Set<R>
}

private object EmptyIdentitySet : IdentitySet<Nothing> {
    override val size: Int = 0

    override fun isEmpty(): Boolean = true

    override fun contains(element: Nothing): Boolean = false

    override fun containsAll(elements: Collection<Nothing>): Boolean = false

    override fun iterator(): Iterator<Nothing> =
        object : Iterator<Nothing> {
            override fun next(): Nothing = throw NoSuchElementException()

            override fun hasNext(): Boolean = false
        }

    override fun <R : Comparable<R>> maxBy(selector: (Nothing) -> R): Nothing = throw NoSuchElementException()

    override fun <R : Any> toSet(): Set<R> = emptySet()
}

@Suppress("UNCHECKED_CAST")
fun <K : Any> emptyIdentitySet(): IdentitySet<K> =
    EmptyIdentitySet as IdentitySet<K>

fun <K : Any> identitySetOf(vararg elements: K): IdentitySet<K> =
    MutableIdentitySet<K>().apply { elements.forEach { add(it) } }

fun <K : Any> IdentitySet<K>.plus(other: IdentitySet<K>): IdentitySet<K> =
    MutableIdentitySet<K>()
        .apply {
            iterator()
                .forEach { add(it) }
            other.iterator()
                .forEach { add(it) }
        }

class MutableIdentitySet<K : Any>(
    vararg elements: K,
) : IdentitySet<K>, MutableIterable<K> {

    private val map: IdentityHashMap<K, Boolean> = IdentityHashMap()

    init {
        elements.forEach { map[it] = true }
    }

    fun <R> map(action: (K) -> R): List<R> =
        map.keys.map(action)

    override fun <R : Comparable<R>> maxBy(selector: (K) -> R): K =
        map.keys.maxBy(selector)!!

    fun add(element: K): Boolean =
        if (map.containsKey(element))
            false
        else
            true.also { map[element] = true }

    fun addAll(elements: Collection<K>): Boolean {
        var result = false
        elements
            .forEach {
                if (add(it))
                    result = true
            }
        return result
    }

    fun clear() =
        map.clear()

    override fun iterator(): MutableIterator<K> =
        object : MutableIterator<K> {
            val base = map.iterator()
            override fun remove() =
                base.remove()

            override fun next(): K =
                base.next().key

            override fun hasNext(): Boolean =
                base.hasNext()
        }

    override fun <R : Any> toSet(): Set<R> =
        buildSet { addAll(map.keys) }

    fun remove(element: K): Boolean =
        map.remove(element) != null

    fun removeAll(elements: Collection<K>): Boolean {
        var result = false
        elements
            .forEach {
                if (remove(it))
                    result = true
            }
        return result
    }

    fun retainAll(elements: Collection<K>): Boolean {
        var result = false
        map
            .keys
            .forEach {
                if (it !in elements)
                    remove(it)
                        .also { result = true }
            }
        return result
    }

    override val size: Int = map.size

    override fun isEmpty(): Boolean =
        map.isEmpty()

    override fun contains(element: K): Boolean =
        map.containsKey(element)

    override fun containsAll(elements: Collection<K>): Boolean =
        elements.all { contains(it) }

}
