package gg.scala.spigot.filter

import gg.scala.spigot.ScalaSpigot
import gg.scala.spigot.logger.Logger

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
object EventFilterHandler
{
    private val filters = mutableMapOf<Class<*>, Boolean>()

    fun initialLoad()
    {
        ScalaSpigot.config.disabledEvents.forEach {
            try
            {
                filters[Class.forName(it)] = false
            } catch (exception: Exception)
            {
               Logger.send("Failed to disable $it", Logger.LogLevel.SEVERE)
            }
        }
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
