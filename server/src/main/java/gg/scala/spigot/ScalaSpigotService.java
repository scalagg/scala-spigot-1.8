package gg.scala.spigot;

import net.minecraft.server.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.*;

/**
 * @author GrowlyX
 * @since 9/18/2022
 */
public class ScalaSpigotService {

    private static final ExecutorService NBT_FILE_SAVER = Executors
        .newSingleThreadExecutor(
            new ScalaSpigotThreadFactory(Thread.NORM_PRIORITY - 2, "NBTFileSaver")
        );

    public static boolean isImportantEntity(Entity entity) {
        return (entity instanceof EntityArrow || entity instanceof EntityPlayer ||
            entity instanceof EntityProjectile || entity instanceof EntityFireball ||
            entity instanceof EntityWither || entity instanceof EntityEnderCrystal ||
            entity instanceof EntityEnderDragon || entity instanceof EntityGhast ||
            entity instanceof EntityFireworks || entity instanceof EntityComplexPart ||
            entity instanceof EntityWeather);
    }

    public static void shutdown()
    {
        NBT_FILE_SAVER.shutdown();

        try {
            if (!NBT_FILE_SAVER.awaitTermination(5, TimeUnit.SECONDS)) {
                MinecraftServer.LOGGER.warn("ScalaSpigot was unable to complete NBT file save processes.");
            }
        } catch (InterruptedException e) {

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
