package org.bukkit.command.defaults;

import com.destroystokyo.paper.util.VersionFetcher;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionCommand extends BukkitCommand {
    private VersionFetcher versionFetcher;
    private Component versionMessage;

    public void setVersion() {
        this.versionMessage = LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&fThis server is running ScalaSpigot version git-scala-spigot-1.8-\"unknown\" (MC: 1.8.8) (Implementing API version 1.8.8-R0.3-SNAPSHOT)");
    }

    public VersionCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/version [plugin name]";
        this.setPermission("bukkit.command.version");
        this.setAliases(Arrays.asList("ver", "about"));
    }

    public void setVersionMessage(Component versionMessage) {
        this.versionMessage = versionMessage;
    }

    private VersionFetcher getVersionFetcher() { // lazy load because unsafe isn't available at command registration
        if (versionFetcher == null) {
            versionFetcher = Bukkit.getUnsafe().getVersionFetcher();
        }

        return versionFetcher;
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        setVersion();

        if (args.length == 0) {
            // Paper start
            if (Bukkit.getVersion().startsWith("null") || versionMessage == null) { // running from ide?
                setVersionMessage(Component.text("Unknown version, custom build?", net.kyori.adventure.text.format.NamedTextColor.YELLOW));

                sender.sendMessage(
                        LegacyComponentSerializer.legacySection()
                                .serialize(versionMessage)
                );
                return true;
            }

            sender.sendMessage(
                    LegacyComponentSerializer.legacySection()
                            .serialize(versionMessage)
            );
        } else {
            StringBuilder name = new StringBuilder();
            boolean found = false;

            for (String arg : args) {
                if (name.length() > 0) {
                    name.append(' ');
                }

                if (!arg.equalsIgnoreCase("author"))
                {
                    name.append(arg);
                }
            }

            String pluginName = name.toString().toLowerCase();

            if (args.length == 1)
            {
                Plugin exactPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (exactPlugin != null) {
                    describeToSender(exactPlugin, sender);
                    return true;
                }

                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getName().toLowerCase().contains(pluginName)) {
                        describeToSender(plugin, sender);
                        found = true;
                    }
                }
            } else
            {
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (!plugin.getDescription().getAuthors().isEmpty())
                    {
                        if (plugin.getDescription().getAuthors().get(0).toLowerCase().contains(pluginName)) {
                            describeToSender(plugin, sender);
                            found = true;
                        }
                    }
                }
            }

            if (!found) {
                sender.sendMessage("This server is not running any plugin by that name.");
                sender.sendMessage("Use /plugins to get a list of plugins.");
            }
        }

        return true;
    }

    private void describeToSender(Plugin plugin, CommandSender sender) {
        PluginDescriptionFile desc = plugin.getDescription();
        sender.sendMessage(ChatColor.GREEN + desc.getName() + ChatColor.WHITE + " version " + ChatColor.GREEN + desc.getVersion());

        if (desc.getDescription() != null) {
            sender.sendMessage(ChatColor.WHITE + desc.getDescription());
            sender.sendMessage("");
        }

        if (desc.getWebsite() != null) {
            sender.sendMessage(ChatColor.WHITE + "Website: " + ChatColor.GREEN + desc.getWebsite());
        }

        if (!desc.getAuthors().isEmpty()) {
            if (desc.getAuthors().size() == 1) {
                sender.sendMessage(ChatColor.WHITE + "Author: " + ChatColor.GREEN + getAuthors(desc));
            } else {
                sender.sendMessage(ChatColor.WHITE + "Authors: " + ChatColor.GREEN + getAuthors(desc));
            }
        }
    }

    private String getAuthors(final PluginDescriptionFile desc) {
        StringBuilder result = new StringBuilder();
        List<String> authors = desc.getAuthors();

        for (int i = 0; i < authors.size(); i++) {
            if (result.length() > 0) {
                if (i < authors.size() - 1) {
                    result.append(", ");
                } else {
                    result.append(" and ");
                }
            }

            result
                    .append(ChatColor.GREEN)
                    .append(authors.get(i))
                    .append(ChatColor.WHITE);
        }

        return result.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            // ScalaSpigot start
            if (!sender.hasPermission("scalaspigot.tab-complete")) {
                return ImmutableList.of();
            }
            // ScalaSpigot end

            List<String> completions = new ArrayList<String>();
            String toComplete = args[0].toLowerCase();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (StringUtil.startsWithIgnoreCase(plugin.getName(), toComplete)) {
                    completions.add(plugin.getName());
                }
            }
            return completions;
        }
        return ImmutableList.of();
    }

}
