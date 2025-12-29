package bps.ipr.common

/**
 * This class is not thread-safe.
 */
class LinkedList<T> {

    private var head: Node<T>? = null
    private var _size: Int = 0

    val size: Int get() = _size

    fun add(element: T): Boolean =
        true
            .also {
                head = head.addToBeginning(element)
                _size++
            }

    fun isEmpty(): Boolean =
        _size == 0

    operator fun contains(element: T): Boolean =
        head?.contains(element) ?: false

    operator fun iterator(): Iterator<T> =
        object : Iterator<T> {

            var current: Node<T>? = head

            override fun next(): T =
                if (current == null)
                    throw NoSuchElementException()
                else {
                    current!!
                        .value
                        .also {
                            current = current?.next
                        }
                }

            override fun hasNext(): Boolean =
                current != null
        }

    operator fun get(index: Int): T =
        when (index) {
            0 ->
                head
                    ?.value
                    ?: throw IndexOutOfBoundsException()
            else ->
                head
                    ?.next
                    ?.getOrNull(index - 1)
                    ?: throw IndexOutOfBoundsException()
        }

}

