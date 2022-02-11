package gg.scala.minetick;

import net.minecraft.server.World;

public class AutoSaveWorldData {

    private long lastAutoSaveTimeStamp;
    private int autoSaveChunkCount;
    private final World world;

    public AutoSaveWorldData(World world) {
        this.world = world;
        this.setLastAutosaveTimeStamp();
    }

    public void setLastAutosaveTimeStamp() {
        try
        {
            this.lastAutoSaveTimeStamp = this
                    .world.worldData.getTime();
        } catch (Exception ignored)
        {
            this.lastAutoSaveTimeStamp = 0L;
        }

        this.autoSaveChunkCount = 0;
    }

    public long getLastAutosaveTimeStamp() {
        return this.lastAutoSaveTimeStamp;
    }

    public void addAutoSaveChunkCount(int count) {
        this.autoSaveChunkCount += count;
    }

    public int getAutoSaveChunkCount() {
        return this.autoSaveChunkCount;
    }
}
