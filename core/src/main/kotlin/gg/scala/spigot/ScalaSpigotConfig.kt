package gg.scala.spigot

import gg.scala.spigot.preset.knockback.KnockbackPreset
import gg.scala.spigot.preset.potion.PotionPreset

/**
 * @author GrowlyX
 * @since 11/1/2021
 */
class ScalaSpigotConfig
{
    val kickForInvalidArmAnimation = true
    val disableUnloadedChunkMovements = true

    val doDayCycle = true

    val trackPluginScoreboards = false
    val automaticChunkUnload = true
    val armorStandTicking = false
    val handleBlockOperations = false
    val handleEntityActivation = false
    val handleMobIntelligence = false
    val handlePlayerDataSave = false
    val handleSpamDisconnect = false
    val adaptiveChunkGarbageCollection = false
    val hidePlayersFromTabList = false
    val blockBreakDesyncFix = true
    val doArmorStandEntityLookups = true

    val userCacheSaveOnShutdown = true
    val userCacheCacheCap = 1000

    val optimizeTntMovement = true
    val optimizeLiquidExplosions = true

    val fishingRodMultiplier = 1.0
    val playerTimeStatisticsInterval = 20
    val trackerUpdateDistance = 1
    val nbtReadLimit = 50000L

    val userAuthenticatorCorePoolLimit = 0
    val userAuthenticatorThreadLimit = 16
    val userAuthenticatorKeepAliveTime = 60L

    val knockbackConfigs = mutableListOf<KnockbackPreset>()
    val potionConfigs = mutableListOf<PotionPreset>()
}
