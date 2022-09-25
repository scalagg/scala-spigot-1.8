package gg.scala.spigot.preset.potion

import gg.scala.spigot.preset.Preset
import kotlinx.serialization.Serializable

@Serializable
class PotionPreset(
    override val id: String,
    override var details: List<String>,

    // TODO: 11/4/2021 no idea what these values stand for, look at the definition
    var speed: Float,
    var distance: Float,
    var offset: Float
) : Preset
