package bps.ipr.common

/**
 * This class is not thread-safe.
 */
class LinkedList<T>(
    head: Node<T>?,
) : Collection<T> {

    constructor(
        vararg elements: T,
    ) : this(
        elements.fold(null as Node<T>?) { acc, element ->
            acc
                ?.addToBeginning(element)
                ?: Node(element, null)
        },
    )

    private var head: Node<T>? = null
    private var _size: Int = 0

    override val size: Int get() = _size

    init {
        this.head = head
//        elements
//            .forEach {
//                add(it)
//            }
    }

    fun add(element: T): Boolean =
        true
            .also {
                head = head.addToBeginning(element)
                _size++
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
        elements.all { contains(it) }

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

