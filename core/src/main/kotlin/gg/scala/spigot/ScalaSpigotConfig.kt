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
    val hidePlayersFromTabList = false
    val smoothTeleportation = true

    val doDayCycle = true

    val automaticChunkUnload = true
    val handleBlockOperations = false
    val handleEntityActivation = false
    val handleMobIntelligence = false
    val handlePlayerDataSave = false

    val fishingRodMultiplier = 1.0

    val knockbackConfigs = mutableListOf<KnockbackPreset>()
    val potionConfigs = mutableListOf<PotionPreset>()

    val disabledEvents = mutableListOf(
        "AirClickEvent"
    )
}
