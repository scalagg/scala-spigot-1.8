package net.minecraft.server;

import net.minecraft.server.Block;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.BlockStone;
import net.minecraft.server.EntitySilverfish;
import net.minecraft.server.ItemStack;
import net.minecraft.server.World;
import net.minecraft.server.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Random;

public class BlockMonsterEggs extends Block {

    public static final BlockStateEnum<net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient> VARIANT = BlockStateEnum.of("variant", net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.class);

    public BlockMonsterEggs() {
        super(Material.CLAY);
        this.j(this.blockStateList.getBlockData().set(VARIANT, net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.STONE));
        this.c(0.0F);
        this.a(CreativeModeTab.c);
    }

    public int a(Random random) {
        return 0;
    }

    public static boolean d(IBlockData iblockdata) {
        Block block = iblockdata.getBlock();

        return iblockdata == Blocks.STONE.getBlockData().set(BlockStone.VARIANT, BlockStone.EnumStoneVariant.STONE) || block == Blocks.COBBLESTONE || block == Blocks.STONEBRICK;
    }

    protected ItemStack i(IBlockData iblockdata) {
        switch (net.minecraft.server.BlockMonsterEggs.SyntheticClass_1.a[((net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient) iblockdata.get(VARIANT)).ordinal()]) {
        case 1:
            return new ItemStack(Blocks.COBBLESTONE);

        case 2:
            return new ItemStack(Blocks.STONEBRICK);

        case 3:
            return new ItemStack(Blocks.STONEBRICK, 1, BlockSmoothBrick.EnumStonebrickType.MOSSY.a());

        case 4:
            return new ItemStack(Blocks.STONEBRICK, 1, BlockSmoothBrick.EnumStonebrickType.CRACKED.a());

        case 5:
            return new ItemStack(Blocks.STONEBRICK, 1, BlockSmoothBrick.EnumStonebrickType.CHISELED.a());

        default:
            return new ItemStack(Blocks.STONE);
        }
    }

    public void dropNaturally(World world, BlockPosition blockposition, IBlockData iblockdata, float f, int i) {
        if (!world.isClientSide && world.getGameRules().getBoolean("doTileDrops")) {
            EntitySilverfish entitysilverfish = new EntitySilverfish(world);

            entitysilverfish.setPositionRotation((double) blockposition.getX() + 0.5D, (double) blockposition.getY(), (double) blockposition.getZ() + 0.5D, 0.0F, 0.0F);
            world.addEntity(entitysilverfish, SpawnReason.SILVERFISH_BLOCK); // CraftBukkit - add SpawnReason
            entitysilverfish.y();
        }

    }

    public int getDropData(World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getType(blockposition);

        return iblockdata.getBlock().toLegacyData(iblockdata);
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(VARIANT, net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.a(i));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient) iblockdata.get(VARIANT)).a();
    }

    public BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] {VARIANT});
    }

    static class SyntheticClass_1 {

        static final int[] a = new int[net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.values().length];

        static {
            try {
                a[net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.COBBLESTONE.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                a[net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.STONEBRICK.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                a[net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.MOSSY_STONEBRICK.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                a[net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.CRACKED_STONEBRICK.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                a[net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient.CHISELED_STONEBRICK.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

        }
    }

    public static enum EnumMonsterEggVarient implements INamable {

        STONE(0, "stone") {;
            public IBlockData d() {
                return Blocks.STONE.getBlockData().set(BlockStone.VARIANT, BlockStone.EnumStoneVariant.STONE);
            }
        }, COBBLESTONE(1, "cobblestone", "cobble") {;
    public IBlockData d() {
        return Blocks.COBBLESTONE.getBlockData();
    }
}, STONEBRICK(2, "stone_brick", "brick") {;
    public IBlockData d() {
        return Blocks.STONEBRICK.getBlockData().set(BlockSmoothBrick.VARIANT, BlockSmoothBrick.EnumStonebrickType.DEFAULT);
    }
}, MOSSY_STONEBRICK(3, "mossy_brick", "mossybrick") {;
    public IBlockData d() {
        return Blocks.STONEBRICK.getBlockData().set(BlockSmoothBrick.VARIANT, BlockSmoothBrick.EnumStonebrickType.MOSSY);
    }
}, CRACKED_STONEBRICK(4, "cracked_brick", "crackedbrick") {;
    public IBlockData d() {
        return Blocks.STONEBRICK.getBlockData().set(BlockSmoothBrick.VARIANT, BlockSmoothBrick.EnumStonebrickType.CRACKED);
    }
}, CHISELED_STONEBRICK(5, "chiseled_brick", "chiseledbrick") {;
    public IBlockData d() {
        return Blocks.STONEBRICK.getBlockData().set(BlockSmoothBrick.VARIANT, BlockSmoothBrick.EnumStonebrickType.CHISELED);
    }
};

        private static final net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient[] g = new net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient[values().length];
        private final int h;
        private final String i;
        private final String j;

        private EnumMonsterEggVarient(int i, String s) {
            this(i, s, s);
        }

        private EnumMonsterEggVarient(int i, String s, String s1) {
            this.h = i;
            this.i = s;
            this.j = s1;
        }

        public int a() {
            return this.h;
        }

        public String toString() {
            return this.i;
        }

        public static net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient a(int i) {
            if (i < 0 || i >= g.length) {
                i = 0;
            }

            return g[i];
        }

        public String getName() {
            return this.i;
        }

        public String c() {
            return this.j;
        }

        public abstract IBlockData d();

        public static net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient a(IBlockData iblockdata) {
            net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient[] ablockmonstereggs_enummonstereggvarient = values();
            int i = ablockmonstereggs_enummonstereggvarient.length;

            for (int j = 0; j < i; ++j) {
                net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient blockmonstereggs_enummonstereggvarient = ablockmonstereggs_enummonstereggvarient[j];

                if (iblockdata == blockmonstereggs_enummonstereggvarient.d()) {
                    return blockmonstereggs_enummonstereggvarient;
                }
            }

            return STONE;
        }

        EnumMonsterEggVarient(int i, String s, net.minecraft.server.BlockMonsterEggs.SyntheticClass_1 blockmonstereggs_syntheticclass_1) {
            this(i, s);
        }

        EnumMonsterEggVarient(int i, String s, String s1, net.minecraft.server.BlockMonsterEggs.SyntheticClass_1 blockmonstereggs_syntheticclass_1) {
            this(i, s, s1);
        }

        static {
            net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient[] ablockmonstereggs_enummonstereggvarient = values();
            int i = ablockmonstereggs_enummonstereggvarient.length;

            for (int j = 0; j < i; ++j) {
                net.minecraft.server.BlockMonsterEggs.EnumMonsterEggVarient blockmonstereggs_enummonstereggvarient = ablockmonstereggs_enummonstereggvarient[j];

                g[blockmonstereggs_enummonstereggvarient.a()] = blockmonstereggs_enummonstereggvarient;
            }

        }
    }
}
