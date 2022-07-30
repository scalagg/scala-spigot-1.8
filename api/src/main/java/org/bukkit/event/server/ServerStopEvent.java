package org.bukkit.event.server;

import org.bukkit.event.HandlerList;

/**
 * @author GrowlyX
 * @since 5/2/2022
 */
public class ServerStopEvent extends ServerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
