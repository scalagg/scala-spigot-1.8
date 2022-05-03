package gg.scala.spigot.logger

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
object Logger
{
    private const val RESET = "\u001B[0m"

    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"

    /**
     * Log a message to console
     * with an optional [LogLevel].
     */
    @JvmStatic
    @JvmOverloads
    fun send(
        message: String, level: LogLevel = LogLevel.INFO
    )
    {
        java.util.logging.Logger.getAnonymousLogger().info(
            "${level.color ?: ""}[Scala] [${level.description}] $message$RESET"
        )
    }

    enum class LogLevel(
        val color: String?, val description: String
    )
    {
        SEVERE(RED, "Error"), WARNING(YELLOW, "Warning"),
        INFO(null, "Info"), SUCCESS(GREEN, "Success")
    }
}
