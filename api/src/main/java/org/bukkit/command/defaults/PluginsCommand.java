package org.bukkit.command.defaults;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PluginsCommand extends BukkitCommand {
    public PluginsCommand(String name) {
        super(name);
        this.description = "Gets a list of plugins running on the server";
        this.usageMessage = "/plugins";
        this.setPermission("bukkit.command.plugins");
        this.setAliases(Arrays.asList("pl"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;
        final Component component = this.getPluginList();

        sender.sendMessage(
            LegacyComponentSerializer.legacySection().serialize(component)
        );

        return true;
    }

    private Component getPluginList() {
        Component pluginList = Component.text("");

        List<Plugin> plugins = Arrays
            .stream(Bukkit.getPluginManager().getPlugins())
            .sorted(Comparator.comparing(Plugin::getName))
            .collect(Collectors.toList());

        int added = 0;

        for (Plugin plugin : plugins) {
            if (added > 0) {
                pluginList = pluginList.append(
                    Component.text(", ")
                        .color(NamedTextColor.WHITE)
                );
            }

            final Component component = Component
                .text(plugin.getDescription().getName())
                .color(
                    plugin.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED
                );

            pluginList = pluginList
                .append(
                    component.hoverEvent(
                        HoverEvent.showText(
                            Component.text("Version: ")
                                .color(NamedTextColor.YELLOW)
                                .append(
                                    Component.text(plugin.getDescription().getVersion())
                                )
                                .color(NamedTextColor.GREEN)
                        )
                    )
                );

            added += 1;
        }

        return Component
            .text("Plugins (" + plugins.size() + "): ")
            .append(pluginList);
    }

    // Spigot Start
    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException
    {
        return java.util.Collections.emptyList();
    }
    // Spigot End
}
