package gg.scala.spigot.server.command

import gg.scala.spigot.ScalaSpigot
import gg.scala.spigot.preset.KnockbackHandler
import gg.scala.spigot.preset.knockback.KnockbackPreset
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

/**
 * @author GrowlyX
 * @since 11/3/2021
 */
class SpigotReloadCommand : BukkitCommand("spigot-reload")
{
    init
    {
        permission = Bukkit.getName().lowercase() + ".command.reload"
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String,
        args: Array<String>
    ): Boolean
    {
        if (!sender.hasPermission(Bukkit.getName().lowercase() + ".command.reload"))
        {
            sender.sendMessage("${ChatColor.RED}You do not have permission to perform this command!")
            return true
        }

        ScalaSpigot.reloadConfig(Bukkit.getWorldContainer())

        sender.sendMessage(ChatColor.GREEN.toString() + "[" + Bukkit.getName() + "]" + ChatColor.GRAY + " The configuration file has been reloaded.")
        return true
    }

}
