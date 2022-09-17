package gg.scala.server.command;

import net.minecraft.server.EntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.spigotmc.SpigotConfig;

public class NoTrackCommand extends Command {

    public NoTrackCommand() {
        super("notrack");
        this.usageMessage = "/notrack <world name> <view distance>";
        this.description = "Modifies the no track distance for a world";
        this.setPermission("scalaspigot.command.notrack");
        this.setPermissionMessage(SpigotConfig.unknownCommandMessage);
    }

    @Override
    public boolean execute(CommandSender sender, String arg1, String[] args) {
        if (!sender.hasPermission("scalaspigot.command.notrack"))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to perform this command!");
            return true;
        }

        if (args != null && args.length == 2) {
            String worldName = args[0];
            String newNTR = args[1];
            int trackRange = -1;
            try {
                trackRange = Integer.parseInt(newNTR);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Error: " + newNTR + " is not a valid integer!");
                return false;
            }
            trackRange = Math.max(trackRange, 0);
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                CraftWorld craftworld = (CraftWorld) world;
                EntityTracker entityTracker = craftworld.getHandle().getTracker();
                entityTracker.setNoTrackDistance(trackRange);
                sender.sendMessage(ChatColor.GREEN + "Set track distance for " + worldName + " to " + trackRange + " blocks.");
            } else {
                sender.sendMessage(ChatColor.RED + "Error: World " + worldName + " not found.");
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.usageMessage + "\n" + ChatColor.RED + this.description);
        }
        return false;
    }

}
