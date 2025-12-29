package bps.ipr.common

interface Queue<T: Any> {
    fun enqueue(element: T)
    fun dequeueOrNull(): T?
}

const val QUEUE_TYPE = "linked"

fun <T : Any> queue(type: String = QUEUE_TYPE): Queue<T> =
    when (type) {
        QUEUE_TYPE -> LinkedQueue<T>()
        else -> throw IllegalArgumentException("Unknown queue type: $type")
    }

class LinkedQueue<T : Any> : Queue<T>{
    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    private var _size: Int = 0

    val size: Int get() = _size

    override fun enqueue(element: T) {
        _size++
        if (head === null) {
            head = Node(element, null)
            tail = head
        } else {
            tail = tail.addToEnd(element)
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
