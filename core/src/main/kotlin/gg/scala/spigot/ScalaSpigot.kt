package gg.scala.spigot

import com.akuleshov7.ktoml.Toml
import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.watch.FixedIntervalWatchable
import com.sksamuel.hoplite.watch.ReloadableConfig
import gg.scala.spigot.filter.EventFilterHandler
import gg.scala.spigot.logger.Logger
import gg.scala.spigot.preset.KnockbackHandler
import gg.scala.spigot.preset.PotionHandler
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.time.seconds

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

    var onReload: () -> Unit = {}

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

        val file = File(parent, "scspigot.toml")

        if (!file.exists())
        {
            file.createNewFile()
        }

        Files.asCharSink(file, Charsets.UTF_8)
            .write(
                Toml.encodeToString(this.config)
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
        val file = File(parent, "scala-spigot.json")

        // old json config functionality
        if (file.exists())
        {
            val reader = Files.newReader(file, Charsets.UTF_8)
            config = GSON.fromJson(reader, ScalaSpigotConfig::class.java)

            file.delete()

            Logger.send("Loaded outdated spigot configuration from scala-spigot.json, saving newest one now. Reload functionality will be resumed on restart.")

            CompletableFuture
                .runAsync {
                    this.saveConfig(parent)
                }
            return
        }

        val tomlFile = File("scspigot.toml")

        if (!tomlFile.exists())
        {
            saveConfig(parent)
        }

        val watcher = FixedIntervalWatchable(10_000L)

        val loader = ConfigLoaderBuilder.default()
            .addSource(PropertySource.resource("/scspigot.toml"))
            .build()

        val reloader = ReloadableConfig(loader, ScalaSpigotConfig::class)
            .addWatcher(watcher)

        this.config = reloader.getLatest()

        reloader.subscribe {
            this.config = it
            onReload.invoke()
        }

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
