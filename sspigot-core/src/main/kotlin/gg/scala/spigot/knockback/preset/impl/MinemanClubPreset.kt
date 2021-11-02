package gg.scala.spigot.knockback.preset.impl

import gg.scala.spigot.knockback.configuration.KnockbackConfig
import gg.scala.spigot.knockback.preset.KnockbackPreset

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
object MinemanClubPreset : KnockbackPreset
{
    override fun id(): String = "Mineman Club"

    override fun details(): List<String> = listOf(
        "Updates the values of",
        "a specified kb-config to",
        "Mineman Club's knockback",
        "values.",
        "",
        "Note: This may not be accurate."
    )

    override fun apply(config: KnockbackConfig)
    {
        TODO("Not yet implemented")
    }
}
