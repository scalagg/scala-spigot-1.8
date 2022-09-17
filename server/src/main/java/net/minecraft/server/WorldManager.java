package net.minecraft.server;

import net.minecraft.server.BlockPosition;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;
import net.minecraft.server.*;

import java.util.Iterator;

public class WorldManager implements IWorldAccess {

    private MinecraftServer a;
    private WorldServer world;

    public WorldManager(MinecraftServer minecraftserver, WorldServer worldserver) {
        this.a = minecraftserver;
        this.world = worldserver;
    }

    public void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {}

    public void a(Entity entity) {
        this.world.getTracker().track(entity);
    }

    public void b(Entity entity) {
        this.world.getTracker().untrackEntity(entity);
        this.world.getScoreboard().a(entity);
    }

    public void a(String s, double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit - this.world.dimension
        this.world.playerMap.sendPacketNearby(null, d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1)); // Manny
    }

    public void a(EntityHuman entityhuman, String s, double d0, double d1, double d2, float f, float f1) {
        if (s.equals("random.drink") || s.contains("step") || s.contains("player") || s.equals("random.eat")) {
            this.world.playerMap.sendPacketNearby((EntityPlayer) entityhuman, d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1)); // Manny
        } else {
            this.a.getPlayerList().sendPacketNearbyIncludingSelf(entityhuman, d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, this.world.dimension, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1));
        }
    }

    public void a(int i, int j, int k, int l, int i1, int j1) {}

    public void a(BlockPosition blockposition) {
        this.world.getPlayerChunkMap().flagDirty(blockposition);
    }

    public void b(BlockPosition blockposition) {}

    public void a(String s, BlockPosition blockposition) {}

    public void a(EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {
        if (i == 2001) {
            this.world.playerMap.sendPacketNearby((EntityPlayer) entityhuman, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 64.0D, new PacketPlayOutWorldEvent(i, blockposition, j, false)); // Manny
        } else {
            this.a.getPlayerList().sendPacketNearbyIncludingSelf(entityhuman, (double) blockposition.getX(), (double)
                    blockposition.getY(), (double) blockposition.getZ(), 64.0D, this.world.dimension, new
                    PacketPlayOutWorldEvent(i, blockposition, j, false));
        }
    }

    public void a(int i, BlockPosition blockposition, int j) {
        this.a.getPlayerList().sendAll(new PacketPlayOutWorldEvent(i, blockposition, j, true));
    }

    public void b(int i, BlockPosition blockposition, int j) {
        // Manny start - PlayerMap
        Entity entity = world.a(i); // PAIL Rename getEntity
        EntityPlayer player = entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
        this.world.playerMap.sendPacketNearby(player, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 32.0D, new PacketPlayOutBlockBreakAnimation(i, blockposition, j));
        // Manny end
    }
}
