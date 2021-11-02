package gg.scala.spigot.filter

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
object EventFilterHandler
{
    private val filters = mutableMapOf<Class<*>, Boolean>()

    fun shouldCall(event: Class<*>): Boolean
    {
        return filters[event] ?: true
    }

    fun registerFilter(clazz: Class<*>, filter: Boolean)
    {
        filters[clazz] = filter
    }

    fun forgetFilter(clazz: Class<*>)
    {
        filters.remove(clazz)
    }
}
