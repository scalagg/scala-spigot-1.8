package gg.scala.spigot

import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import gg.scala.spigot.filter.EventFilterHandler
import gg.scala.spigot.logger.Logger
import gg.scala.spigot.preset.KnockbackHandler
import gg.scala.spigot.preset.PotionHandler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import java.io.File
import java.util.concurrent.CompletableFuture


/**
 * @author GrowlyX
 * @since 11/1/2021
 */
object ScalaSpigot
{
    @JvmStatic
    val GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create()

    private val toml = Toml {
        ignoreUnknownKeys = true
    }

    var config = ScalaSpigotConfig()

    private val initialization = System.currentTimeMillis()

    var onReload: () -> Unit = {}

    fun initialLoad()
    {
        Logger.send("Starting ScalaSpigot initialization...")
        EventFilterHandler.initialLoad()

        Logger.send("ScalaSpigot has initialized. (${System.currentTimeMillis() - initialization}ms)")
    }

    fun saveConfig(parent: File)
    {
        kotlin.runCatching {
            KnockbackHandler.save(parent)
            PotionHandler.save(parent)
        }.onFailure {
            Logger.send("Failed to save knockback and/or potion configs", Logger.LogLevel.SEVERE)
            it.printStackTrace()
        }

        val file = File(parent, "scspigot.toml")

        if (!file.exists())
        {
            file.createNewFile()
        }

        Files.asCharSink(file, Charsets.UTF_8)
            .write(
                toml.encodeToString(this.config)
            )
    }

    @JvmOverloads
    fun reloadConfig(
        parent: File, silently: Boolean = false
    ): Boolean
    {
        if (!silently)
        {
            Logger.send("Attempting a reload of scspigot.toml...")
        }

        try
        {
            internalLoadConfig(parent)
        } catch (exception: Exception)
        {
            if (!silently)
            {
                Logger.send("Failed to reload scspigot.toml!")
            }

            return false
        }

        return true
    }

    fun internalLoadConfig(parent: File)
    {
        kotlin.runCatching {
            KnockbackHandler.load(parent)
            PotionHandler.load(parent)
        }.onFailure {
            Logger.send("Failed to load knockback and/or potion configs", Logger.LogLevel.SEVERE)
            it.printStackTrace()
        }

        val file = File(parent, "scala-spigot.json")

        // old json config functionality
        if (file.exists())
        {
            val reader = Files.newReader(file, Charsets.UTF_8)
            config = GSON.fromJson(reader, ScalaSpigotConfig::class.java)

            kotlin.runCatching {
                reader.close()
            }

            file.delete()

            Logger.send("Loaded outdated spigot configuration from scala-spigot.json, saving newest one now. Reload functionality will be resumed on restart.")

            CompletableFuture
                .runAsync {
                    this.saveConfig(parent)
                }
            return
        }

        val tomlFile = File(parent, "scspigot.toml")

        if (!tomlFile.exists())
        {
            saveConfig(parent)
        }

        this.config = toml.decodeFromString(tomlFile.readText())

        CompletableFuture
            .runAsync {
                // save new config model
                this.saveConfig(parent)
            }
            .thenRun {
                Logger.send("Saved latest toml config model.")
            }
    }
}
