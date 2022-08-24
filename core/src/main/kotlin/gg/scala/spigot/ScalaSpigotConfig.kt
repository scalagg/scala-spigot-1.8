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

    val automaticChunkUnload = true
    val handleBlockOperations = false
    val handleEntityActivation = false
    val handleMobIntelligence = false
    val handlePlayerDataSave = false
    val handleSpamDisconnect = false
    val adaptiveChunkGarbageCollection = false

    val optimizeTntMovement = true
    val optimizeLiquidExplosions = true

    val fishingRodMultiplier = 1.0
    val playerTimeStatisticsInterval = 20

    // https://github.com/TitanVale/TitaniumMC/blob/6d124af19debe401b4c9d34579b6daaa6f77ea19/patches/server/0079-Implement-Nacho-Prevent-most-NBT-related-overflow-ex.patch#L40
    val nbtReadLimiter = 50000L

    val knockbackConfigs = mutableListOf<KnockbackPreset>()
    val potionConfigs = mutableListOf<PotionPreset>()

    val disabledEvents = mutableListOf(
        "AirClickEvent"
    )
}
