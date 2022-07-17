package gg.scala.spigot.preset.knockback

import gg.scala.spigot.preset.Preset

open class KnockbackPreset(
    override val id: String,
    override var details: List<String>,
    var friction: Double,
    var horizontal: Double,
    var vertical: Double,
    var verticalLimit: Double,
    var extraHorizontal: Double,
    var extraVertical: Double,
    var slowdown: Double = 0.6,
    var rodVerticalMultiplier: Double = 0.0,
    var rodHorizontalMultiplier: Double = 0.0,
) : Preset
