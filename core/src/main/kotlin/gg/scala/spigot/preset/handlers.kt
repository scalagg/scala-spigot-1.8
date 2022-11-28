package gg.scala.spigot.preset

import gg.scala.spigot.ScalaSpigot
import gg.scala.spigot.preset.knockback.KnockbackPreset
import gg.scala.spigot.preset.potion.PotionPreset
import java.io.File

object KnockbackHandler : PresetHandler<KnockbackPreset>()
{
    override fun getId() = "knockback"

    fun findPreset(id: String): KnockbackPreset? {
        return presets.find { id.equals(it.id, true) }
    }

    data class PresetContainer(
        val presets: List<KnockbackPreset>
    )

    override fun unwrap(file: File)
    {
        presets.addAll(
            ScalaSpigot.GSON
                .fromJson(
                    file.readText(),
                    PresetContainer::class.java
                )
                .presets
        )
    }

    override fun persist(file: File)
    {
        file.writeText(
            ScalaSpigot.GSON.toJson(PresetContainer(presets))
        )
    }

    override fun initialLoad()
    {
        if (this.presets.isEmpty())
        {
            this.presets.add(
                KnockbackPreset(
                    "default", listOf("The default preset"),
                    horizontalFriction = 2.0,
                    verticalFriction = 2.0,
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

object PotionHandler : PresetHandler<PotionPreset>()
{
    override fun getId() = "potions"

    data class PresetContainer(
        val presets: List<PotionPreset>
    )

    override fun unwrap(file: File)
    {
        presets.addAll(
            ScalaSpigot.GSON
                .fromJson(
                    file.readText(),
                    PresetContainer::class.java
                )
                .presets
        )
    }

    override fun persist(file: File)
    {
        file.writeText(
            ScalaSpigot.GSON.toJson(PresetContainer(presets))
        )
    }

    fun findPreset(id: String): PotionPreset? {
        return presets.find { id.equals(it.id, true) }
    }

    override fun initialLoad()
    {
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
