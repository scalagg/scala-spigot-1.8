package gg.scala.spigot.preset

import gg.scala.spigot.ScalaSpigot
import java.io.File

open class PresetHandler<T : Preset> {

    val presets = mutableListOf<T>()

    lateinit var activePreset: T

    open fun initialLoad() {

    }

    fun getId() = ""

    fun load(parent: File)
    {
        val file = File(parent, "${getId()}.sc")

        if (file.exists())
        {
            presets.addAll(
                ScalaSpigot.GSON.fromJson<List<T>>(
                    file.readText(), List::class.java
                )
            )

            initialLoad()
            return
        }

        initialLoad()
        save(parent)
    }

    fun save(parent: File)
    {
        val file = File(parent, "${getId()}.sc")

        if (!file.exists())
        {
            file.createNewFile()
        }

        file.writeText(
            ScalaSpigot.GSON.toJson(presets)
        )
    }

    fun findPreset(id: String): T? {
        return presets.find { id.equals(it.id, true) }
    }
}
