package gg.scala.spigot.server.command

import gg.scala.spigot.ScalaSpigot
import gg.scala.spigot.preset.KnockbackHandler
import gg.scala.spigot.preset.PotionHandler
import gg.scala.spigot.preset.potion.PotionPreset
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

/**
 * @author GrowlyX
 * @since 11/3/2021
 */
class PotionCommand : BukkitCommand("potion")
{
    init
    {
        aliases = listOf("pot", "pots", "potions")
        permission = Bukkit.getName().lowercase() + ".command.potion"
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String,
        args: Array<String>
    ): Boolean
    {
        if (!sender.hasPermission(Bukkit.getName().lowercase() + ".command.potion"))
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
                    if (PotionHandler.findPreset(args[1]) != null)
                    {
                        sender.sendMessage("This profile already exists.")
                        return true
                    }

                    val preset = PotionPreset(
                        id = "default",
                        details = listOf(""),
                        offset = -20F,
                        splashTick = 5,
                        speed = 0.5F,
                        distance = 0.05F,
                        hitbox = 1.0F
                    )

                    PotionHandler.presets.add(preset)

                    sender.sendMessage("Created new profile.")

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
                    val existing = PotionHandler.findPreset(args[1])

                    if (existing != null)
                    {
                        PotionHandler.presets.remove(existing)

                        ScalaSpigot.saveConfig(Bukkit.getWorldContainer())
                        sender.sendMessage("Deleted profile.")
                    } else
                    {
                        sender.sendMessage("That profile does not exist.")
                    }
                } else
                {
                    sender.sendMessage("${ChatColor.RED}You must provide a name.")
                }
            }
            "list" ->
            {
                sender.sendMessage("${ChatColor.GOLD}Pot presets")

                KnockbackHandler.presets.forEach {
                    sender.sendMessage(it.id)
                }
            }
            "view" ->
            {
                if (args.size >= 2)
                {
                    val existing = PotionHandler.findPreset(args[1])

                    if (existing != null)
                    {
                        sender.sendMessage(ScalaSpigot.GSON.toJson(existing))
                    } else
                    {
                        sender.sendMessage("not exist")
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
                    val existing = PotionHandler.findPreset(args[1])

                    if (existing != null)
                    {
                        PotionHandler.activePreset = existing
                        sender.sendMessage("set ok")
                    } else
                    {
                        sender.sendMessage("not exist")
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
                    val existing = PotionHandler.findPreset(args[1])

                    if (existing == null)
                    {
                        sender.sendMessage("not exist")
                        return true
                    }

                    val type = args[2]
                    val field = PotionPreset::class.java.declaredFields.firstOrNull {
                        it.name == type
                    }

                    if (field == null)
                    {
                        sender.sendMessage("valid: ")
                        sender.sendMessage(PotionPreset::class.java.declaredFields.joinToString(separator = ", ") {it.name}
                        )
                        return true
                    }

                    try
                    {
                        val value = args[3]
                        field.isAccessible = true

                        if (field.name == "splashTick" && value.toInt() > 5 || value.toInt() < 1) {
                            sender.sendMessage("${ChatColor.RED}You cannot have a ${ChatColor.WHITE}Splash Tick Rate ${ChatColor.RED}higher than 5 or lower than 1.")
                            return true
                        }

                        when (field.type) {
                            Float::class.java -> {
                                field.set(existing, value.toFloat())
                            }
                            Double::class.java -> {
                                field.set(existing, value.toDouble())
                            }
                            Int::class.java -> {
                                field.set(existing, value.toInt())
                            }
                            else -> {
                                sender.sendMessage("Unsupported field type: ${field.type.simpleName}")
                                return true
                            }
                        }

                        sender.sendMessage("Successfully updated field ${field.name} to $value for preset ${args[1]}.")

                        ScalaSpigot.saveConfig(Bukkit.getWorldContainer())
                    } catch (exception: Exception)
                    {
                        sender.sendMessage("sorry was invalid type or something: ${exception.message}")
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
