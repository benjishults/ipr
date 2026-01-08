package bps.ipr.common

data class Node<T : Any>(
    val value: T,
    var next: Node<T>?,
) {

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

}

fun <T : Any> Node<T>?.addToBeginning(element: T): Node<T> =
    Node(element, this)


