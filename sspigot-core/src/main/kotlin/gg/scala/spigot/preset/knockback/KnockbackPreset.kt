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
    var extraHorizontal: Double,
    var extraVertical: Double
) : Preset