package gg.scala.spigot.cleaner

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime

/**
 * @author GrowlyX
 * @since 1/17/2022
 */
object LogCleanup
{
    @JvmStatic
    @JvmOverloads
    fun cleanup(folder: String = "logs")
    {
        val directory = File(folder)

        if (!directory.exists())
            return

        val listed = directory
            .listFiles()?.toList() ?: return

        if (listed.size < 10)
            return

        val files = listed.sortedBy {
            val creationTime = Files
                .getAttribute(
                    it.toPath(), "creationTime"
                ) as FileTime

            return@sortedBy creationTime
                .toMillis()
        }

        files.forEachIndexed { index, file ->
            if (index > 10)
            {
                file.deleteOnExit()
            }
        }
    }
}
