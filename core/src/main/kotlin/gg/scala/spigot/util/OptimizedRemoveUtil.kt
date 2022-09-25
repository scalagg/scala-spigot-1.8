package gg.scala.spigot.util

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
object OptimizedRemoveUtil
{
    interface Marker
    {
        val isNeedRemoval: Boolean

        fun markRemoval()
    }

    /**
     * An optimized remove all
     * From TacoSpigot https://github.com/TacoSpigot/TacoSpigot
     *
     * @param list     the list to remove from
     * @param toRemove what to remove
     * @param position the position to remove from
     * @param <T>      the type being removed
     * @return the new position
     */
    @JvmStatic
    fun <T : Marker> removeAll(list: MutableList<T>, toRemove: Set<T>, position: Int): Int
    {
        var mutable = position

        for (marker in toRemove)
        {
            marker.markRemoval()
        }

        val size = list.size
        var insertAt = 0

        for (i in 0 until size)
        {
            val element: T? = list.getOrNull(i)
            if (i == position) mutable = insertAt
            if (element != null && !element.isNeedRemoval)
            {
                list[insertAt++] = element
            }
        }

        list.subList(insertAt, size).clear()

        return mutable
    }
}
