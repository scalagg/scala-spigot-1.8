package gg.scala.server.threads;

import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadingManager {

    private final Logger log = LogManager.getLogger();
    private ExecutorService nbtFileService = Executors.newSingleThreadExecutor(new NamePriorityThreadFactory(Thread.NORM_PRIORITY - 2, "CraftSpigot_NBTFileSaver"));
    private static ThreadingManager instance;

    public ThreadingManager() {
        instance = this;
    }

    public void shutdown() {
        this.nbtFileService.shutdown();
        while(!this.nbtFileService.isTerminated()) {
            try {
                if(!this.nbtFileService.awaitTermination(3, TimeUnit.MINUTES)) {
                    log.warn("Server is still waiting for NBT Files to be saved.");
                }
            } catch(InterruptedException e) {}
        }
    }

    public static void saveNBTFileStatic(NBTTagCompound compound, File file) {
        instance.saveNBTFile(compound, file);
    }

    public void saveNBTFile(NBTTagCompound compound, File file) {
        this.nbtFileService.execute(new NBTFileSaver(compound, file));
    }

    private class NBTFileSaver implements Runnable {

        private NBTTagCompound compound;
        private File file;

        public NBTFileSaver(NBTTagCompound compound, File file) {
            this.compound = compound;
            this.file = file;
        }

        public void run() {
            FileOutputStream fileoutputstream = null;
            try {
                fileoutputstream = new FileOutputStream(this.file);
                NBTCompressedStreamTools.a(this.compound, (OutputStream) fileoutputstream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(fileoutputstream != null) {
                    try {
                        fileoutputstream.close();
                    } catch (IOException e) {}
                }
            }
            this.compound = null;
            this.file = null;
        }
    }
}
