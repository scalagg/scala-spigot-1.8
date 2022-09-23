package gg.scala

import org.bukkit.craftbukkit.help.HelpYamlReader
import java.util.*

/**
 * @author GrowlyX
 * @since 7/30/2022
 */
object ScalaSpigotServerProperties
{
    private val properties = Properties()
        .apply {
            this.load(
                HelpYamlReader.loadVersion()
            )
        }

    val version by lazy {
        this.properties.getProperty("version")
    }

    val branch by lazy {
        this.properties.getProperty("branch")
    }
}
