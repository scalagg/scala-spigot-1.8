package gg.scala.spigot.preset.knockback

open class KnockbackPreset(
    val id: String,
    var details: List<String>,
    var horizontalFriction: Double,
    var verticalFriction: Double,
    var horizontal: Double,
    var vertical: Double,
    var verticalLimit: Double,
    var extraHorizontal: Double,
    var extraVertical: Double,
    var slowdown: Double = 0.6,
    var rodVerticalMultiplier: Double = 0.0,
    var rodHorizontalMultiplier: Double = 0.0,
    var distanceKB: Boolean = true,
    var autoWTap: Boolean = false
)
