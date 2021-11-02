package gg.scala.spigot.knockback.preset

import gg.scala.spigot.knockback.configuration.KnockbackConfig

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
interface KnockbackPreset
{
    fun id(): String
    fun details(): List<String>

    fun apply(config: KnockbackConfig)
}
