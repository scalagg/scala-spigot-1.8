package com.destroystokyo.paper.console;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.command.CraftConsoleCommandSender;

/**
 * @author GrowlyX
 * @since 12/28/2021
 */
public class TerminalConsoleCommandSender extends CraftConsoleCommandSender {

    private static final Logger LOGGER = MinecraftServer.LOGGER;

    @Override
    public void sendRawMessage(String message) {
        // TerminalConsoleAppender supports color codes directly in log messages
        LOGGER.info(message);
    }

}
