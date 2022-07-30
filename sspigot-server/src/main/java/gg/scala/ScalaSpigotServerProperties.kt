package gg.scala

import org.bukkit.craftbukkit.Main
import java.util.*

/**
 * @author GrowlyX
 * @since 7/30/2022
 */
object ScalaSpigotServerProperties
{
    private val properties = Properties()
        .apply {
            val resource = Main::class.java
                .getResourceAsStream("version.properties")!!

            this.load(resource)
        }

    val version by lazy {
        this.properties.getProperty("version")
    }
}
