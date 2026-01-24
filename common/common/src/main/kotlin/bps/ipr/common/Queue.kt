package bps.ipr.common

interface Queue<T : Any> {
    fun enqueue(element: T)
    fun dequeueOrNull(): T?
}

const val QUEUE_TYPE = "linked"

fun <T : Any> queue(type: String = QUEUE_TYPE): Queue<T> =
    when (type) {
        QUEUE_TYPE -> LinkedQueue<T>()
        else -> throw IllegalArgumentException("Unknown queue type: $type")
    }

class LinkedQueue<T : Any> : Queue<T>, Collection<T> {
    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    private var _size: Int = 0

    override val size: Int get() = _size

    override fun isEmpty(): Boolean =
        _size == 0

    override fun contains(element: T): Boolean =
        head?.contains(element) ?: false

    override fun iterator(): Iterator<T> =
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

    override fun enqueue(element: T) {
        _size++
        if (head === null) {
            head = Node(element, null)
            tail = head
        } else {
            Node(element, null)
                .also {
                    tail!!.next = it
                    tail = it
                }
        }
    }

    override fun dequeueOrNull(): T? =
        head
            ?.also {
                head = it.next
                if (head === null)
                    tail = null
                _size--
            }
            ?.value

}
