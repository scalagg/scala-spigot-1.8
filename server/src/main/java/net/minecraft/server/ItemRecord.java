package net.minecraft.server;

import com.google.common.collect.Maps;
import net.minecraft.server.BlockJukeBox;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.World;
import net.minecraft.server.*;

import java.util.Map;

public class ItemRecord extends Item {

    private static final Map<String, net.minecraft.server.ItemRecord> b = Maps.newHashMap();
    public final String a;

    protected ItemRecord(String s) {
        this.a = s;
        this.maxStackSize = 1;
        this.a(CreativeModeTab.f);
        b.put("records." + s, this);
    }

    public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        IBlockData iblockdata = world.getType(blockposition);

        if (iblockdata.getBlock() == Blocks.JUKEBOX && !((Boolean) iblockdata.get(BlockJukeBox.HAS_RECORD)).booleanValue()) {
            if (world.isClientSide) {
                return true;
            } else {
                // CraftBukkit Start
                /*
                ((BlockJukeBox) Blocks.JUKEBOX).a(world, blockposition, iblockdata, itemstack);
                world.a((EntityHuman) null, 1005, blockposition, Item.getId(this));
                --itemstack.count;
                entityhuman.b(StatisticList.X);
                */
                // CraftBukkit End
                return true;
            }
        } else {
            return false;
        }
    }

    public EnumItemRarity g(ItemStack itemstack) {
        return EnumItemRarity.RARE;
    }
}
