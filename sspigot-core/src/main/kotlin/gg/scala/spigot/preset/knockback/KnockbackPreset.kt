package gg.scala.spigot.preset.knockback

import gg.scala.spigot.preset.Preset

open class KnockbackPreset(
    override val id: String,
    override var details: List<String>,

    // knockback values
    var friction: Double,
    var horizontal: Double,
    var vertical: Double,
    var verticalLimit: Double,
    var horizontalLimit: Double,
    var extraHorizontal: Double,
    var extraVertical: Double,
    var sprintMultiply: Double = 0.256,
    var rodHorizontal: Double = 1.0,
    var rodVertical: Double = 1.0,
) : Preset
