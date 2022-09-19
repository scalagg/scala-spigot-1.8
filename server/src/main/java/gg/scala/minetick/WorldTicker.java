package gg.scala.minetick;

import net.minecraft.server.WorldServer;

public class WorldTicker implements Runnable {

    private final WorldServer worldToTick;
    private static final Object PLAYER_UPDATES_LOCK = new Object();

    public WorldTicker(WorldServer world) {
        this.worldToTick = world;
    }

    @Override
    public void run() {
        try {
            this.worldToTick.tickEntities();
        } catch (Throwable throwable1) {
            throwable1.printStackTrace();
        }

        synchronized (PLAYER_UPDATES_LOCK) {
            this.worldToTick.getTracker().updatePlayers();
        }
    }
}
