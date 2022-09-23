package org.bukkit.craftbukkit.util;

import net.minecraft.server.ExceptionWorldConflict;
import net.minecraft.server.MinecraftServer;

public class ServerShutdownThread extends Thread {
    private final MinecraftServer server;

    public ServerShutdownThread(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            server.stop();
        } catch (ExceptionWorldConflict ex) {
            ex.printStackTrace();
        } finally {
            org.apache.logging.log4j.LogManager.shutdown(); // Paper
            try {
//                net.minecrell.terminalconsole.TerminalConsoleAppender.close(); // Paper - Use TerminalConsoleAppender
            } catch (Exception e) {
            }
        }
    }
}
