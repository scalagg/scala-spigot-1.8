package org.bukkit.craftbukkit.command;

import net.minecraft.server.DedicatedServer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
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

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        CompletionWaiter waiter = new CompletionWaiter(line.line());
        this.server.processQueue.add(waiter);

        // Async Tab Complete
        com.destroystokyo.paper.event.server.AsyncTabCompleteEvent event;
        java.util.List<String> completions = new java.util.ArrayList<>();
        event = new com.destroystokyo.paper.event.server.AsyncTabCompleteEvent(Bukkit.getConsoleSender(), completions, line.line(), true, null);
        event.callEvent();
        completions = event.isCancelled() ? com.google.common.collect.ImmutableList.of() : event.getCompletions();

        if (event.isCancelled() || event.isHandled()) {
            if (!completions.isEmpty()) {
                candidates.addAll(completions.stream().map(Candidate::new).collect(java.util.stream.Collectors.toList()));
            }

            return;
        }

        try {
            List<String> offers = waiter.get(); // wait until completions get processed on main thread

            if (offers != null && offers.size() > 0)
            {
                for (String offer : offers) {
                    if (offer.isEmpty()) continue;

                    candidates.add(new Candidate(offer));
                }
            }
        } catch (ExecutionException e) {
            this.server.server.getLogger().log(Level.WARNING, "Unhandled exception when tab completing", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private class CompletionWaiter extends Waitable<List<String>> {
        private final String buffer;

        private CompletionWaiter(String buffer) {
            this.buffer = buffer;
        }

        @Override
        protected List<String> evaluate() {
            final CraftServer server = ConsoleCommandCompleter.this.server.server;

            return server.getCommandMap().tabComplete(server.getConsoleSender(), this.buffer);
        }
    }
}
