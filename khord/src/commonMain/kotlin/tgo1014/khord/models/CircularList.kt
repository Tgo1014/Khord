package tgo1014.khord.models

internal class CircularList<T>(private val items: List<T>) {

    operator fun get(index: Int): T {
        val adjustedIndex = index.mod(items.size)
        return items[if (adjustedIndex >= 0) adjustedIndex else adjustedIndex + items.size]
    }

}