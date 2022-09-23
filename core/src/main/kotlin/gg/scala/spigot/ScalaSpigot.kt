package gg.scala.spigot

import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import gg.scala.spigot.filter.EventFilterHandler
import gg.scala.spigot.logger.Logger
import gg.scala.spigot.preset.KnockbackHandler
import gg.scala.spigot.preset.PotionHandler
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/1/2021
 */
object ScalaSpigot
{
    @JvmStatic
    val GSON: Gson = GsonBuilder().setPrettyPrinting()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING).create()

    var config = ScalaSpigotConfig()

    private val initialization = System.currentTimeMillis()

    fun initialLoad()
    {
        Logger.send("Starting ScalaSpigot initialization...")

        KnockbackHandler.initialLoad()
        PotionHandler.initialLoad()
        EventFilterHandler.initialLoad()

        Logger.send("ScalaSpigot has initialized. (${System.currentTimeMillis() - initialization}ms)")
    }

    fun saveConfig(parent: File)
    {
        config.knockbackConfigs.clear()
        config.knockbackConfigs.addAll(KnockbackHandler.presets)

        config.potionConfigs.clear()
        config.potionConfigs.addAll(PotionHandler.presets)

        val file = File(parent, "scala-spigot.json")
        Files.write(GSON.toJson(config), file, Charsets.UTF_8)
    }

    @JvmOverloads
    fun reloadConfig(silently: Boolean = false, parent: File): Boolean
    {
        if (!silently)
        {
            Logger.send("Attempting a reload of scala-spigot.json...")
        }

        try
        {
            internalLoadConfig(parent)
        } catch (exception: Exception)
        {
            if (!silently)
            {
                Logger.send("Failed to reload scala-spigot.json!")
            }

            return false
        }

        return true
    }

    fun internalLoadConfig(parent: File)
    {
        val file = File(parent, "scala-spigot.json")

        if (!file.exists())
        {
            file.createNewFile()
                .also {
                    if (it)
                    {
                        val newest = GSON.toJson(config)

                        Files.write(
                            newest.toByteArray(), file
                        )

                        Logger.send("Created scala-spigot.json.")
                    } else
                    {
                        Logger.send("Failed to create scala-spigot.json, using default values.", Logger.LogLevel.SEVERE)
                    }
                }
        }

        val reader = Files.newReader(file, Charsets.UTF_8)
        config = GSON.fromJson(reader, ScalaSpigotConfig::class.java)

        Logger.send("Loaded spigot configuration from scala-spigot.json.")

        CompletableFuture
            .runAsync {
                val configSnapshot = GSON.toJson(config)

                Files.write(
                    configSnapshot.toByteArray(), file
                )
            }
            .thenRun {
                Logger.send("Updated spigot configuration with latest values.")
            }
    }
}
