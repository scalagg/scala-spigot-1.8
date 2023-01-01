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
)
