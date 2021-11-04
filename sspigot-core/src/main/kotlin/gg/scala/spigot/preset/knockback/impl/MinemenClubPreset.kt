package gg.scala.spigot.preset.knockback.impl

import gg.scala.spigot.preset.knockback.KnockbackPreset

class MinemenClubPreset(
    override val id: String = "Minemen Club",
    override var details: List<String> = listOf(
        "Updates the values of",
        "a specified kb-config to",
        "Mineman Club's knockback",
        "values.",
        "",
        "Note: This may not be accurate."
    ),

    // knockback values
    friction: Double,
    horizontal: Double,
    vertical: Double,
    verticalLimit: Double,
    extraHorizontal: Double,
    extraVertical: Double
) : KnockbackPreset(
    id,
    details,
    friction,
    horizontal,
    vertical,
    verticalLimit,
    extraHorizontal,
    extraVertical
)