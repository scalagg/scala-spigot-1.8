package gg.scala.spigot.knockback.configuration

/**
 * @author GrowlyX
 * @since 11/2/2021
 */
data class KnockbackConfigEntry(
    val name: String,
    val description: String,
    val value: Any,
    val type: Class<*>
)
