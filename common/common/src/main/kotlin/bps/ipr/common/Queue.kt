package bps.ipr.common

class Queue<T : Any> {
    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    private var _size: Int = 0

    val size: Int get() = _size

    fun enqueue(element: T) {
        _size++
        if (head === null) {
            head = Node(element, null)
            tail = head
        } else {
            tail = tail.add(element)
        }
    }

    fun dequeue(): T? =
        head
            ?.also {
                head = it.next
                _size--
            }
            ?.value

}
