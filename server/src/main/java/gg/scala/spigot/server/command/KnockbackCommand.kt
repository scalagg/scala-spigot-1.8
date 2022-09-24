package gg.scala.spigot.server.command

import gg.scala.spigot.ScalaSpigot
import gg.scala.spigot.preset.KnockbackHandler
import gg.scala.spigot.preset.knockback.KnockbackPreset
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

/**
 * @author GrowlyX
 * @since 11/3/2021
 */
class KnockbackCommand : BukkitCommand("knockback")
{
    init
    {
        aliases = listOf("kb")
        permission = Bukkit.getName().lowercase() + ".command.knockback"
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String,
        args: Array<String>
    ): Boolean
    {
        if (!sender.hasPermission(Bukkit.getName().lowercase() + ".command.knockback"))
        {
            sender.sendMessage("${ChatColor.RED}You do not have permission to perform this command!")
            return true
        }

        if (args.isEmpty())
        {
            sender.sendMessage("${ChatColor.RED}Usage: /$commandLabel <create|delete|view|list|edit|set> <name>")
            return true
        }

        when (args[0].lowercase())
        {
            "create" ->
            {
                if (args.size >= 2)
                {
                    if (KnockbackHandler.findPreset(args[1]) != null)
                    {
                        sender.sendMessage("${ChatColor.RED}This knockback preset already exists!")
                        return true
                    }

                    val preset = KnockbackPreset(
                        args[1], listOf("Default description."),

                        friction = 2.0,
                        horizontal = 0.35,
                        vertical = 0.35,
                        verticalLimit = 0.4,
                        extraHorizontal = 0.425,
                        extraVertical = 0.085
                    )

                    KnockbackHandler.presets.add(preset)

                    sender.sendMessage("${ChatColor.GREEN}Created knockback preset: ${ChatColor.YELLOW}${args[1]}${ChatColor.GREEN}.")

                    ScalaSpigot.saveConfig(Bukkit.getWorldContainer())
                } else
                {
                    sender.sendMessage("${ChatColor.RED}You must provide a name.")
                }
            }
            "delete" ->
            {
                if (args.size >= 2)
                {
                    val existing = KnockbackHandler.findPreset(args[1])

                    if (existing != null)
                    {
                        KnockbackHandler.presets.remove(existing)

                        ScalaSpigot.saveConfig(Bukkit.getWorldContainer())
                        sender.sendMessage("${ChatColor.RED}Deleted knockback preset: ${ChatColor.YELLOW}${args[1]}${ChatColor.RED}.")
                    } else
                    {
                        sender.sendMessage("${ChatColor.RED}This preset does not exist.")
                    }
                } else
                {
                    sender.sendMessage("${ChatColor.RED}You must provide a name.")
                }
            }
            "list" ->
            {
                sender.sendMessage("${ChatColor.AQUA}Knockback Presets:")

                KnockbackHandler.presets.forEach {
                    sender.sendMessage(" ${it.id}")
                }
            }
            "view" ->
            {
                if (args.size >= 2)
                {
                    val existing = KnockbackHandler.findPreset(args[1])

                    if (existing != null)
                    {
                        sender.sendMessage(ScalaSpigot.GSON.toJson(existing))
                    } else
                    {
                        sender.sendMessage("${ChatColor.RED}This preset does not exist.")
                    }
                } else
                {
                    sender.sendMessage("${ChatColor.RED}You must provide a name.")
                }
            }
            "set" ->
            {
                if (args.size >= 2)
                {
                    val existing = KnockbackHandler.findPreset(args[1])

                    if (existing != null)
                    {
                        KnockbackHandler.activePreset = existing
                        sender.sendMessage("${ChatColor.GREEN}Updated active preset to: ${ChatColor.YELLOW}${args[1]}${ChatColor.GREEN}.")
                    } else
                    {
                        sender.sendMessage("${ChatColor.RED}This preset does not exist.")
                    }
                } else
                {
                    sender.sendMessage("${ChatColor.RED}You must provide a name.")
                }
            }
            "edit" ->
            {
                if (args.size >= 4)
                {
                    val existing = KnockbackHandler.findPreset(args[1])

                    if (existing == null)
                    {
                        sender.sendMessage("${ChatColor.RED}This preset does not exist.")
                        return true
                    }

                    val type = args[2]

                    val field = KnockbackPreset::class.java
                        .declaredFields.firstOrNull {
                            it.name == type
                        }

                    if (field == null)
                    {
                        sender.sendMessage("${ChatColor.RED}Sorry, that's an invalid field.")
                        sender.sendMessage("${ChatColor.RED}The valid fields are shown below: ")
                        sender.sendMessage(KnockbackPreset::class.java.declaredFields.joinToString(separator = ", ") {it.name}
                        )
                        return true
                    }

                    try
                    {
                        val value = args[3]
                        field.isAccessible = true
                        field.set(existing, value.toDouble())

                        sender.sendMessage("${ChatColor.GREEN}Updated field ${ChatColor.WHITE}${field.name}${ChatColor.GREEN} to ${ChatColor.WHITE}${value}${ChatColor.GREEN} for preset ${ChatColor.YELLOW}${args[1]}${ChatColor.GREEN}.")

                        ScalaSpigot.saveConfig(Bukkit.getWorldContainer())
                    } catch (exception: Exception)
                    {
                        sender.sendMessage("${ChatColor.RED}${exception.message}")
                    }
                } else
                {
                    sender.sendMessage("${ChatColor.RED}You must provide a name, type, and value.")
                }
            }
        }

        return true
    }

}
