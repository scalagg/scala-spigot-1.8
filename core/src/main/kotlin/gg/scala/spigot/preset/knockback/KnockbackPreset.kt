package gg.scala.spigot.preset.knockback

open class KnockbackPreset(
    val id: String,
    var details: List<String>,
    var Friction: Double,
    var horizontal: Double,
    var vertical: Double,
    var verticalLimit: Double,
    var extraHorizontal: Double,
    var extraVertical: Double,
    var slowdown: Double = 0.6,
    var autoWTap: Boolean = false,
    var fixWTap: Boolean = false,
    var distanceCheck: Boolean = true,
    var rodHorizontalMultiplier: Double = 1.0,
    var rodVerticalMultiplier: Double = 1.0,
    var bowHorizontalMultiplier: Double = 0.8,
    var bowVerticalMultiplier: Double = 0.95,
)
