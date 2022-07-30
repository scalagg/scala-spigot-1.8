package net.minecraft.server;

import net.minecraft.server.BlockPosition;
import net.minecraft.server.CommandBlockListenerAbstract;
import net.minecraft.server.Entity;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;
import net.minecraft.server.*;

public class TileEntityCommand extends TileEntity {

    private final CommandBlockListenerAbstract a = new CommandBlockListenerAbstract() {
        {
            sender = new org.bukkit.craftbukkit.command.CraftBlockCommandSender(this); // CraftBukkit - add sender
        }
        public BlockPosition getChunkCoordinates() {
            return net.minecraft.server.TileEntityCommand.this.position;
        }

        public Vec3D d() {
            return new Vec3D((double) net.minecraft.server.TileEntityCommand.this.position.getX() + 0.5D, (double) net.minecraft.server.TileEntityCommand.this.position.getY() + 0.5D, (double) net.minecraft.server.TileEntityCommand.this.position.getZ() + 0.5D);
        }

        public World getWorld() {
            return net.minecraft.server.TileEntityCommand.this.getWorld();
        }

        public void setCommand(String s) {
            super.setCommand(s);
            net.minecraft.server.TileEntityCommand.this.update();
        }

        public void h() {
            net.minecraft.server.TileEntityCommand.this.getWorld().notify(net.minecraft.server.TileEntityCommand.this.position);
        }

        public Entity f() {
            return null;
        }
    };

    public TileEntityCommand() {}

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        this.a.a(nbttagcompound);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a.b(nbttagcompound);
    }

    public Packet getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.b(nbttagcompound);
        return new PacketPlayOutTileEntityData(this.position, 2, nbttagcompound);
    }

    public boolean F() {
        return true;
    }

    public CommandBlockListenerAbstract getCommandBlock() {
        return this.a;
    }

    public CommandObjectiveExecutor c() {
        return this.a.n();
    }
}
