package gg.scala.spigot.preset

import gg.scala.spigot.ScalaSpigot
import gg.scala.spigot.preset.knockback.KnockbackPreset
import gg.scala.spigot.preset.knockback.impl.MinemenClubPreset
import gg.scala.spigot.preset.potion.PotionPreset

object KnockbackHandler : PresetHandler<KnockbackPreset>() {
    override fun initialLoad() {
        ScalaSpigot.config.knockbackConfigs.forEach {
            this.presets.add(it)
        }

        if (this.presets.isEmpty())
        {
            this.presets.add(
                KnockbackPreset(
                    "default", listOf(""),
                    friction = 2.0,
                    horizontal = 0.35,
                    vertical = 0.35,
                    verticalLimit = 0.4,
                    extraHorizontal = 0.425,
                    extraVertical = 0.085
                )
            )
        }

        this.activePreset = this.presets[0]
    }
}
object PotionHandler : PresetHandler<PotionPreset>() {
    override fun initialLoad() {
        ScalaSpigot.config.potionConfigs.forEach {
            this.presets.add(it)
        }

        if (this.presets.isEmpty())
        {
            this.presets.add(
                PotionPreset(
                    id = "default",
                    details = listOf(
                        ""
                    ),
                    offset = -20F,
                    speed = 0.5F,
                    distance = 0.05F
                )
            )
        }

        this.activePreset = this.presets[0]
    }
}
