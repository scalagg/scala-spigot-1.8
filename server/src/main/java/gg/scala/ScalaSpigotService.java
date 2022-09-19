package gg.scala;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author GrowlyX
 * @since 9/18/2022
 */
public class ScalaSpigotService {

    private static final ExecutorService NBT_FILE_SAVER = Executors
        .newSingleThreadExecutor(
            new ScalaSpigotThreadFactory(Thread.NORM_PRIORITY - 2, "ScalaSpigotService")
        );

    public static void shutdown()
    {
        NBT_FILE_SAVER.shutdown();

        while (!NBT_FILE_SAVER.isTerminated()) {
            try {
                if (!NBT_FILE_SAVER.awaitTermination(3, TimeUnit.MINUTES)) {
                    MinecraftServer.LOGGER.warn("ScalaSpigot is still waiting for NBT Files to be saved.");
                }
            } catch (InterruptedException e) {

            }
        }
    }

    public static void saveNBTFileStatic(NBTTagCompound compound, File file) {
        saveNBTFile(compound, file);
    }

    public static void saveNBTFile(NBTTagCompound compound, File file) {
        NBT_FILE_SAVER.submit(new NBTFileSaver(compound, file));
    }

    private static class NBTFileSaver implements Callable<Object> {

        private NBTTagCompound compound;
        private File file;

        public NBTFileSaver(NBTTagCompound compound, File file) {
            this.compound = compound;
            this.file = file;
        }

        public Object call() {

            try (FileOutputStream fileoutputstream = new FileOutputStream(this.file)) {
                NBTCompressedStreamTools.a(this.compound, fileoutputstream);
            } catch (Exception exception) {
                MinecraftServer.LOGGER.error("Error \"" + exception.getMessage() + "\" while saving file: " + this.file.getAbsolutePath());
                exception.printStackTrace();
            }

            this.compound = null;
            this.file = null;

            return null;
        }
    }
}
