package gg.scala.spigot.filter

import gg.scala.spigot.mock.AirClickEvent

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
object EventFilterHandler
{
    private val filters = mutableMapOf<Class<*>, Boolean>()

    fun initialLoad()
    {
        filters[AirClickEvent::class.java] = false
    }

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
