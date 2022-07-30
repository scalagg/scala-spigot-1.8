package gg.scala.spigot.preset

open class PresetHandler<T : Preset> {

    val presets = mutableListOf<T>()

    lateinit var activePreset: T

    open fun initialLoad() {

    }

    fun findPreset(id: String): T? {
        return presets.find { id.equals(it.id, true) }
    }
}
