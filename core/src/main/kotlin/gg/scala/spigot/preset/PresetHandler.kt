package gg.scala.spigot.preset

import gg.scala.spigot.ScalaSpigot
import java.io.File

open class PresetHandler<T : Any>
{
    val presets = mutableListOf<T>()

    lateinit var activePreset: T

    open fun initialLoad()
    {

    }

    open fun getId() = ""

    open fun unwrap(file: File)
    {

    }

    fun load(parent: File)
    {
        val file = File(parent, "${getId()}.sc")

        if (file.exists())
        {
            unwrap(file)
            initialLoad()
            return
        }

        initialLoad()
        save(parent)
    }

    open fun persist(file: File)
    {

    }

    fun save(parent: File)
    {
        val file = File(parent, "${getId()}.sc")

        if (!file.exists())
        {
            file.createNewFile()
        }

        persist(file)
    }
}
