package gg.scala.spigot.preset

import gg.scala.spigot.preset.knockback.KnockbackPreset
import gg.scala.spigot.preset.knockback.impl.MinemenClubPreset
import gg.scala.spigot.preset.potion.PotionPreset

object KnockbackHandler : PresetHandler<KnockbackPreset>() {
    override fun initialLoad() {
        this.presets.add(
            MinemenClubPreset(
                friction = 2.0,
                horizontal = 0.35,
                vertical = 0.35,
                verticalLimit = 0.4,
                extraHorizontal = 0.425,
                extraVertical = 0.085
            )
        )
        this.activePreset = this.presets[0]
    }
}
object PotionHandler : PresetHandler<PotionPreset>() {
    override fun initialLoad() {
        this.presets.add(
            PotionPreset(
                id = "retardpots",
                details = listOf(
                    ""
                ),
                offset = 0.5F,
                speed = 500F,
                distance = -50F
            )
        )

        this.activePreset = this.presets[0]
    }
}