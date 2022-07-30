package gg.scala.minetick;

import org.bukkit.Bukkit;
import org.bukkit.event.world.WorldSaveEvent;

import net.minecraft.server.ExceptionWorldConflict;
import net.minecraft.server.FileIOThread;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegionFileCache;
import net.minecraft.server.WorldServer;

public class AutoSaveJob {

    private WorldServer worldserver;
    private final JobDetail jobDetail;

    public AutoSaveJob(JobDetail detail, WorldServer worldserver) {
        this.jobDetail = detail;
        this.worldserver = worldserver;
    }

    public boolean process() throws ExceptionWorldConflict {
        if (this.isJob(JobDetail.WORLD_SAVE) && this.worldserver != null) {
            MinecraftServer.getServer().info("[AutoSave] Saving world " + this.worldserver.getWorld().getName());
            this.worldserver.save(true, null);
            FileIOThread.a().setNoDelay(true);
        } else if (this.isJob(JobDetail.WORLD_SAVE_EVENT) && this.worldserver != null) {
            if (FileIOThread.a().isDone()) {
                FileIOThread.a().setNoDelay(false);
                RegionFileCache.a();
                Bukkit.getPluginManager().callEvent(new WorldSaveEvent(this.worldserver.getWorld()));
            } else {
                return false;
            }
        }
        this.worldserver = null;
        return true;
    }

    private boolean isJob(JobDetail detail) {
        if (this.jobDetail != null) {
            return this.jobDetail.equals(detail);
        }
        return false;
    }

    public enum JobDetail {
        WORLD_SAVE,
        WORLD_SAVE_EVENT,
    }
}
