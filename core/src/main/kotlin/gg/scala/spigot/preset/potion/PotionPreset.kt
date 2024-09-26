package gg.scala.spigot.preset.potion

class PotionPreset(
    val id: String,
    var details: List<String>,

    // TODO: 11/4/2021 no idea what these values stand for, look at the definition
    var speed: Float,
    var distance: Float,
    var offset: Float,
    var splashTick: Int,
    var hitbox: Float,
)
