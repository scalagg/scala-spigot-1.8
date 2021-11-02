package gg.scala.spigot

import gg.scala.spigot.knockback.configuration.KnockbackConfig

/**
 * @author GrowlyX
 * @since 11/1/2021
 */
class ScalaSpigotConfig
{
    val kickForInvalidArmAnimation = true
    val hidePlayersFromTabList = false
    val automaticChunkUnload = true
    val handleBlockOperations = false
    val handleEntityActivation = false

    val knockbackConfigs = listOf<KnockbackConfig>()
}
