package org.bukkit.craftbukkit.command;

import net.minecraft.server.DedicatedServer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class ConsoleCommandCompleter implements Completer {
    private final DedicatedServer server;

    public ConsoleCommandCompleter(DedicatedServer server) {
        this.server = server;
    }

    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        final CraftServer server = this.server.server;
        final String buffer = line.line();
        // Paper end

        Waitable<List<String>> waitable = new Waitable<List<String>>() {
            @Override
            protected List<String> evaluate() {
                return server.getCommandMap().tabComplete(server.getConsoleSender(), buffer);
            }
        };

        server.getServer().processQueue.add(waitable);

        try {
            List<String> offers = waitable.get();
            if (offers == null) {
                return;
            }

            // Paper start - JLine update
            for (String completion : offers) {
                if (completion.isEmpty()) {
                    continue;
                }

                candidates.add(new Candidate(completion));
            }
            // Paper end
        } catch (ExecutionException e) {
            server.getLogger().log(Level.WARNING, "Unhandled exception when tab completing", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
