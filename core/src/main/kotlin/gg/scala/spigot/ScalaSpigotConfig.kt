package gg.scala.spigot

import gg.scala.spigot.preset.knockback.KnockbackPreset
import gg.scala.spigot.preset.potion.PotionPreset

/**
 * @author GrowlyX
 * @since 11/1/2021
 */
data class ScalaSpigotConfig(
    val configVersion: Int = 0,
    val kickForInvalidArmAnimation: Boolean = true,
    val disableUnloadedChunkMovements: Boolean = true,

    val doDayCycle: Boolean = true,

    val trackPluginScoreboards: Boolean = false,
    val automaticChunkUnload: Boolean = true,
    val armorStandTicking: Boolean = false,
    val handleBlockOperations: Boolean = false,
    val handleEntityActivation: Boolean = false,
    val handleMobIntelligence: Boolean = false,
    val handlePlayerDataSave: Boolean = false,
    val handleSpamDisconnect: Boolean = false,
    val adaptiveChunkGarbageCollection: Boolean = false,
    val hidePlayersFromTabList: Boolean = false,
    val blockBreakDesyncFix: Boolean = true,
    val doArmorStandEntityLookups: Boolean = true,
    val namespacedCommandsEnabled: Boolean = true,

    val userCacheSaveOnShutdown: Boolean = true,
    val userCacheCacheCap: Int = 1000,

    val optimizeTntMovement: Boolean = true,
    val optimizeLiquidExplosions: Boolean = true,

    val fishingRodMultiplier: Double = 1.0,
    val playerTimeStatisticsInterval: Int = 20,
    val nbtReadLimit: Long = 50000L,

    val userAuthenticatorCorePoolLimit: Int = 0,
    val userAuthenticatorThreadLimit: Int = 16,
    val userAuthenticatorKeepAliveTime: Long = 60L,

    val autoInstalledPlugins: AutoInstalledPlugins = AutoInstalledPlugins(),
    val sentrySettings: SentrySettings = SentrySettings(),
    val dimensionSettings: DimensionSettings = DimensionSettings(),

    val knockbackConfigs: MutableList<KnockbackPreset> = mutableListOf(),
    val potionConfigs: MutableList<PotionPreset> = mutableListOf()
)
{
    data class AutoInstalledPlugins(
        var installSpark: Boolean = true
    )

    data class SentrySettings(
        var initialize: Boolean = false,
        var dsn: String = ""
    )

    data class DimensionSettings(
        var configureEnd: Boolean = false,
        var configureNether: Boolean = false,
    )
}
