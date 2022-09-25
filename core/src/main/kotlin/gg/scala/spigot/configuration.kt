package gg.scala.spigot

import com.akuleshov7.ktoml.annotations.TomlComments
import gg.scala.spigot.preset.knockback.KnockbackPreset
import gg.scala.spigot.preset.potion.PotionPreset
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * @author GrowlyX
 * @since 11/1/2021
 */
@Serializable
data class ScalaSpigotConfig(
    @TomlComments(
        "You're viewing the primary configuration file for ScalaSpigot.",
        " * Please report any issues to a Scala team member.",
        "",
        "Kick when an invalid arm animation is detected."
    )
    val kickForInvalidArmAnimation: Boolean = true,
    @TomlComments(
        "Don't allow movements in unloaded chunks",
        " * Removes the weird floating down animation when moving in unloaded chunks.",
        " * Users won't be able to get out of the unloaded chunk if this is set to true."
    )
    val disableUnloadedChunkMovements: Boolean = false,

    @TomlComments(
        "Enables day cycle loop when enabled."
    )
    val doDayCycle: Boolean = true,

    @TomlComments(
        "On servers with plugins that constantly churn through scoreboards, there is a risk of",
        "degraded GC performance due to the number of scoreboards held on by weak references.",
        "Most plugins don't even need the (vanilla) functionality that requires all plugin",
        "scoreboards to be tracked by the server. Instead, only track scoreboards when an",
        "objective is added with a non-dummy criteria. (credits to astei for writing this patch)",
    )
    val trackPluginScoreboards: Boolean = false,
    @TomlComments(
        "Unloads unused chunks when enabled",
        " * Quite unnecessary on servers with static maps. (hubs, minigames, etc.)"
    )
    val automaticChunkUnload: Boolean = true,
    @TomlComments(
        "Disables armor stand ticking when enabled.",
        " * Most servers don't require this functionality.",
        " * Should enable when there are many armor stand entities (Minequest).",
    )
    val armorStandTicking: Boolean = false,
    val armorStandEntityLookupsEnabled: Boolean = true,
    @TomlComments(
        "Enables block ticking when enabled.",
        " * Most servers don't require this functionality.",
    )
    val handleBlockOperations: Boolean = false,
    @TomlComments(
        "Enables entity activation when enabled & in the proper range of the targeted entity.",
    )
    val handleEntityActivation: Boolean = false,
    @TomlComments(
        "Enables entity AI when enabled.",
    )
    val handleMobIntelligence: Boolean = false,
    @TomlComments(
        "Enables player data saving when enabled.",
    )
    val handlePlayerDataSave: Boolean = true,
    @TomlComments(
        "Enables the \"disconnect.spam\" kick functionality when enabled.",
        " * This functionality is disabled by default for operators."
    )
    val handleSpamDisconnect: Boolean = false,
    @TomlComments(
        "Enables adaptcive chunk garbage collection based on player count."
    )
    val adaptiveChunkGarbageCollection: Boolean = false,
    @TomlComments(
        "Enables namespaced commands when enabled (/plugin:commandlabel)",
        " * These are shown during tab completion."
    )
    val namespacedCommandsEnabled: Boolean = true,
    @TomlComments(
        "Miscellaneous fixes & toggles."
    )
    val hidePlayersFromTabList: Boolean = false,
    val blockBreakDesyncFix: Boolean = true,

    @TomlComments(
        "User cache persistence settings:"
    )
    val userCacheSaveOnShutdown: Boolean = true,
    val userCacheCacheCap: Int = 1000,

    @TomlComments(
        "Various (possibly) game breaking optimizations and/or changes."
    )
    val optimizeTntMovement: Boolean = true,
    val optimizeLiquidExplosions: Boolean = true,

    val fishingRodMultiplier: Double = 1.0,
    val playerTimeStatisticsInterval: Int = 20,

    @TomlComments(
        "Sets the NBT read limit; used for NBTTagCompounds.",
        " * Should keep default."
    )
    val nbtReadLimit: Long = 50000L,

    @TomlComments(
        "User authenticator thread-pool settings:",
        " * Should keep default."
    )
    val userAuthenticatorCorePoolLimit: Int = 0,
    val userAuthenticatorThreadLimit: Int = 16,
    val userAuthenticatorKeepAliveTime: Long = 60L,

    @TomlComments(
        "Adds auto-updated, default installed plugins.",
        " * Currently supported:",
        "   - spark: Profiler by lucko."
    )
    val autoInstalledPlugins: AutoInstalledPlugins = AutoInstalledPlugins(),
    @TomlComments(
        "Exception reporter functionality for https://sentry.io.",
    )
    val sentrySettings: SentrySettings = SentrySettings(),
    @TomlComments(
        "Very self-explanatory. Moved from other configuration files to make lives easier.",
    )
    val dimensionSettings: DimensionSettings = DimensionSettings(),

    @TomlComments(
        "Knockback & potion configurations.",
    )
    val knockbackConfigs: MutableList<KnockbackPreset> = mutableListOf(),
    val potionConfigs: MutableList<PotionPreset> = mutableListOf()
)

@Serializable
data class AutoInstalledPlugins(
    var installSpark: Boolean = true
)

@Serializable
data class SentrySettings(
    var initialize: Boolean = false,
    var dsn: String = ""
)

@Serializable
data class DimensionSettings(
    var configureEnd: Boolean = false,
    var configureNether: Boolean = false,
)
