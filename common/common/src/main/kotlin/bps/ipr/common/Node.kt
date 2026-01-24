package bps.ipr.common

data class Node<T : Any>(
    val value: T,
    var next: Node<T>? = null,
) : Iterable<T> {

    fun contains(element: T): Boolean =
        value == element ||
                (next?.contains(element) ?: false)

//    fun every(predicate: (T) -> Boolean): Boolean =
//        predicate(value) && (next?.every(predicate) ?: true)

    fun getOrNull(index: Int): T? =
        when (index) {
            0 -> value
            else -> next?.getOrNull(index - 1)
        }

    fun indexOf(element: T): Int =
        if (value == element)
            0
        else
            next
                ?.indexOf(element)
                ?.let { it + 1 }
                ?: -1

    fun forEach(action: (T) -> Unit) {
        action(value)
        next?.forEach(action)
    }

    fun deepCopy(): Node<T> =
        Node(value, next?.deepCopy())

    fun toList(): List<T> =
        buildList {
            this@Node.forEach {
                add(it)
            }
        }

    fun toSet(): Set<T> =
        buildSet {
            this@Node.forEach {
                add(it)
            }
        }

    fun toIdentitySet(): IdentitySet<T> =
        MutableIdentitySet<T>().apply {
            this@Node.forEach {
                add(it)
            }
        }

    override fun iterator(): Iterator<T> =
        object : Iterator<T> {
            /**
             * The [Node] containing the next element to be returned by [next].
             */
            private var current: Node<T>? = this@Node
            override fun next(): T =
                current
                    ?.value
                    ?.also { current = current?.next }
                    ?: throw NoSuchElementException()

            override fun hasNext(): Boolean =
                current !== null
        }

}

fun <T : Any> Node<T>?.addToBeginning(element: T): Node<T> =
    Node(element, this)


