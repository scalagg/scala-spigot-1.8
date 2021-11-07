package gg.scala.spigot.preset.knockback.impl

import gg.scala.spigot.preset.knockback.KnockbackPreset

class MinemenClubPreset(
    override val id: String = "default",
    override var details: List<String> = listOf(
        "default"
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
