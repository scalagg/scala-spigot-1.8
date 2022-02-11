package gg.scala.minetick;

import net.minecraft.server.*;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.ArrayDeque;
import java.util.Queue;

public class AutoSave {

    private final Queue<WorldServer> levelAndMapsQueue;
    private final Queue<WorldServer> saveChunksQueue;
    private final Queue<WorldServer> unloadChunksQueue;
    private final Queue<WorldServer> eventQueue;

    private AutoSaveStep step;

    private long fileioStart;
    private long fileioEnd;

    private int regionFileCount;

    private long regionFileCacheStart;
    private long regionFileCacheEnd;

    private int chunkQueuedCount;

    public AutoSave() {
        this.levelAndMapsQueue = new ArrayDeque<>();
        this.saveChunksQueue = new ArrayDeque<>();
        this.unloadChunksQueue = new ArrayDeque<>();
        this.eventQueue = new ArrayDeque<>();

        this.reset();
    }

    private static String formatLongTime(long duration) {
        return String.format("%.2f", ((double) (duration / 1000000L)) / 1000.0D);
    }

    public void queueWorld(WorldServer worldserver) {
        this.levelAndMapsQueue.add(worldserver);
        this.saveChunksQueue.add(worldserver);
        this.unloadChunksQueue.add(worldserver);
        this.eventQueue.add(worldserver);
    }

    public void reset() {
        this.levelAndMapsQueue.clear();
        this.saveChunksQueue.clear();
        this.unloadChunksQueue.clear();
        this.eventQueue.clear();
        this.step = AutoSaveStep.IDLE;
        this.chunkQueuedCount = 0;
    }

    private void moveToNextStep() {
        this.step = AutoSaveStep.nextStep(this.step);
    }

    public boolean execute() throws ExceptionWorldConflict {
        WorldServer worldServer;

        switch (this.step) {
            default:
            case IDLE:
                break;
            case START:
                MinecraftServer.LOGGER.info("[AutoSave] Started autosave process...");

                for (final WorldServer world : this.saveChunksQueue) {
                    world.getAutoSaveWorldData().setLastAutosaveTimeStamp();
                }

                this.moveToNextStep();
                return this.execute();
            case SAVE_PLAYERS:
                MinecraftServer.getServer()
                        .getPlayerList().savePlayers();

                this.moveToNextStep();
                break;
            case SAVE_LEVEL_AND_MAPS:
                worldServer = this.levelAndMapsQueue.poll();
                if (worldServer != null) {
                    worldServer.saveOnlyLevel(true, null);
                }

                if (this.levelAndMapsQueue.isEmpty()) {
                    this.moveToNextStep();
                }

                break;
            case SAVE_CHUNKS:
                worldServer = this.saveChunksQueue.peek();
                if (worldServer != null) {
                    if (worldServer.saveOnlyChunks(false, null)) {
                        this.chunkQueuedCount += worldServer.getAutoSaveWorldData().getAutoSaveChunkCount();
                        this.saveChunksQueue.poll();
                    }
                }

                if (this.saveChunksQueue.isEmpty()) {
                    this.moveToNextStep();
                }

                break;
            case UNLOAD_CHUNKS:
                worldServer = this.unloadChunksQueue.poll();
                if (worldServer != null) {
                    worldServer.unloadOnlyUnusedChunks(true, null);
                }

                if (this.unloadChunksQueue.isEmpty()) {
                    this.moveToNextStep();
                }

                break;
            case WRITE_FILES_START:
                FileIOThread.a.setNoDelay(true);
                this.fileioStart = System.nanoTime();
                this.moveToNextStep();
                break;
            case WRITE_FILES_WAIT:
                if (FileIOThread.a.isDone()) {
                    this.fileioEnd = System.nanoTime();
                    FileIOThread.a.setNoDelay(false);
                    this.moveToNextStep();
                }
                break;
            case FIRE_WORLDSAVEEVENT:
                this.regionFileCount = RegionFileCache.a.size();
                this.regionFileCacheStart = System.nanoTime();
                RegionFileCache.a();
                this.regionFileCacheEnd = System.nanoTime();

                for (WorldServer worldserver : this.eventQueue) {
                    WorldSaveEvent event = new WorldSaveEvent(worldserver.getWorld());
                    MinecraftServer.getServer().server.getPluginManager().callEvent(event);
                }

                this.moveToNextStep();
                break;
            case FINISHED:
                MinecraftServer.LOGGER.info("[AutoSave] Done. Queued " + this.chunkQueuedCount + " chunks for saving. Took " + formatLongTime(this.fileioEnd - this.fileioStart) + " seconds to write them.");
                MinecraftServer.LOGGER.info("[AutoSave] Cleared " + this.regionFileCount + " cached region files in " + formatLongTime(this.regionFileCacheEnd - this.regionFileCacheStart) + " seconds.");
                this.reset();
                return true;
        }
        return false;
    }

    public boolean isActive() {
        return this.step.isActiveState();
    }

    public void start() {
        this.step = AutoSaveStep.START;
    }

    public enum AutoSaveStep {
        IDLE(false),
        START(),
        SAVE_PLAYERS(),
        SAVE_LEVEL_AND_MAPS(),
        SAVE_CHUNKS(),
        UNLOAD_CHUNKS(),
        WRITE_FILES_START(),
        WRITE_FILES_WAIT(),
        FIRE_WORLDSAVEEVENT(),
        FINISHED();

        private final boolean activeState;

        AutoSaveStep() {
            this(true);
        }

        AutoSaveStep(boolean activeState) {
            this.activeState = activeState;
        }

        public static AutoSaveStep nextStep(AutoSaveStep current) {
            AutoSaveStep[] values = AutoSaveStep.values();
            if (current.ordinal() + 1 < values.length) {
                return values[current.ordinal() + 1];
            }
            return current;
        }

        public boolean isActiveState() {
            return this.activeState;
        }
    }
}
