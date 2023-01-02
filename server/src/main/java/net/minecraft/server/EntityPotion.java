package net.minecraft.server;

import gg.scala.spigot.preset.PotionHandler;
import gg.scala.spigot.preset.potion.PotionPreset;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PotionSplashEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EntityPotion extends EntityProjectile {

    public ItemStack item;
    public EntityPotion(World world) {
        super(world);
    }

    public EntityPotion(World world, EntityLiving entityliving, int i) {
        this(world, entityliving, new ItemStack(Items.POTION, 1, i));
    }

    public EntityPotion(World world, EntityLiving entityliving, ItemStack itemstack) {
        super(world, entityliving);
        this.item = itemstack;
    }

    public EntityPotion(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(world, d0, d1, d2);
        this.item = itemstack;
    }

    protected float m() {
        final PotionPreset preset = PotionHandler
                .INSTANCE.getActivePreset();

        return preset.getDistance();
    }

    protected float j() {
        final PotionPreset preset = PotionHandler
                .INSTANCE.getActivePreset();

        return preset.getSpeed();
    }

    protected float l() {
        final PotionPreset preset = PotionHandler
                .INSTANCE.getActivePreset();

        return preset.getOffset();
    }

    public void setPotionValue(int i) {
        if (this.item == null) {
            this.item = new ItemStack(Items.POTION, 1, 0);
        }
        this.item.setData(i);
    }

    public int getPotionValue() {
        if (this.item == null) {
            this.item = new ItemStack(Items.POTION, 1, 0);
        }
        return this.item.getData();
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isClientSide) {
            Player shooter;
            List<MobEffect> list = Items.POTION.h(this.item);
            AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(4.0, 2.0, 4.0);
            List<EntityLiving> list1 = this.world.a(EntityLiving.class, axisalignedbb);
            Iterator<EntityLiving> iterator = list1.iterator();
            HashMap<LivingEntity, Double> affected = new HashMap<LivingEntity, Double>();
            while (iterator.hasNext()) {
                EntityLiving entityliving = iterator.next();
                double d0 = this.h(entityliving);
                if (!(d0 < 16.0)) continue;
                double d1 = 1.0 - Math.sqrt(d0) / 4.0;
                if (entityliving == movingobjectposition.entity) {
                    d1 = 1.0;
                } else if (Math.hypot(this.motX, this.motZ) > 0.4 && d1 > 0.8) {
                    d1 = 1.0;
                }
                affected.put((LivingEntity)((Object)entityliving.getBukkitEntity()), d1);
            }
            PotionSplashEvent event = CraftEventFactory.callPotionSplashEvent(this, affected);
            if (event.getEntity().getShooter() instanceof Player && (shooter = (Player)event.getEntity().getShooter()).isSprinting() && event.getIntensity(shooter) > 0.5) {
                event.setIntensity(shooter, 1.0);
            }
            if (!event.isCancelled() && list != null && !list.isEmpty()) {
                for (LivingEntity victim : event.getAffectedEntities()) {
                    if (!(victim instanceof CraftLivingEntity)) continue;
                    EntityLiving entityliving = ((CraftLivingEntity)victim).getHandle();
                    if (this.getShooter() != null && entityliving instanceof EntityPlayer && !((EntityPlayer)entityliving).getBukkitEntity().canSeeEntity(this.getShooter().getBukkitEntity())) continue;
                    double d1 = event.getIntensity(victim);
                    for (MobEffect mobeffect : list) {
                        int i = mobeffect.getEffectId();
                        if (!this.world.pvpMode && this.getShooter() instanceof EntityPlayer && entityliving instanceof EntityPlayer && entityliving != this.getShooter() && (i == 2 || i == 4 || i == 7 || i == 15 || i == 17 || i == 18 || i == 19)) continue;
                        if (MobEffectList.byId[i].isInstant()) {
                            MobEffectList.byId[i].applyInstantEffect(this, this.getShooter(), entityliving, mobeffect.getAmplifier(), d1);
                            continue;
                        }
                        int j = (int)(d1 * (double)mobeffect.getDuration() + 0.5);
                        if (j <= 20) continue;
                        entityliving.addEffect(new MobEffect(i, j, mobeffect.getAmplifier()));
                    }
                }
            }
            if (this.getShooter() instanceof EntityHuman) {
                int x = (int)Math.round(this.locX);
                int y = (int)Math.round(this.locY);
                int z = (int)Math.round(this.locZ);
                BlockPosition customLocation = new BlockPosition(x, y, z);
                this.world.a((EntityHuman)this.getShooter(), 2002, customLocation, this.getPotionValue());
            } else {
                this.world.triggerEffect(2002, new BlockPosition(this), this.getPotionValue());
            }
            this.die();
        }
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("Potion", 10)) {
            this.item = ItemStack.createStack(nbttagcompound.getCompound("Potion"));
        } else {
            this.setPotionValue(nbttagcompound.getInt("potionValue"));
        }
        if (this.item == null) {
            this.die();
        }
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.item != null) {
            nbttagcompound.set("Potion", this.item.save(new NBTTagCompound()));
        }
    }
}