package gg.scala.skin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.TileEntitySkull;

import java.util.UUID;

public class CraftSkinCache {

    public void loadAndRun(String name, Runnable runnable)
    {
        TileEntitySkull.b(new GameProfile(UUID.randomUUID(), name), $ -> {
            runnable.run();
            return false;
        });
    }
}
