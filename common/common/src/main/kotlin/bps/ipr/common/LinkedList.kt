package bps.ipr.common

/**
 * This class is not thread-safe.
 */
class LinkedList<T> : List<T> {

    private var head: Node<T>? = null
    private var _size: Int = 0

    override val size: Int get() = _size

    fun toList(): List<T> =
        buildList { addAll(this@LinkedList) }

    fun add(element: T): Boolean =
        true
            .also {
                Node(element, head)
                    .also { head = it }
            }

    override fun isEmpty(): Boolean =
        _size == 0

    override operator fun contains(element: T): Boolean =
        head?.contains(element) ?: false

    override operator fun iterator(): Iterator<T> =
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

    override fun containsAll(elements: Collection<T>): Boolean =
        TODO()

    override operator fun get(index: Int): T =
        when (index) {
            0 -> head?.value ?: throw IndexOutOfBoundsException()
            else -> head?.next?.getOrNull(index - 1) ?: throw IndexOutOfBoundsException()
        }

    override fun indexOf(element: T): Int =
        head?.indexOf(element) ?: -1

    override fun lastIndexOf(element: T): Int =
        TODO()

    override fun listIterator(): ListIterator<T> =
        TODO("Not yet implemented")

    override fun listIterator(index: Int): ListIterator<T> =
        TODO("Not yet implemented")

    override fun subList(fromIndex: Int, toIndex: Int): List<T> =
        TODO("Not yet implemented")
}

