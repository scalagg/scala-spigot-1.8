package gg.scala.spigot.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author GrowlyX
 * @since 10/10/2022
 */
public class ServerLoadedEvent extends Event {

    public static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
