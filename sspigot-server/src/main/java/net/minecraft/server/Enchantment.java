package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract class Enchantment {

    // CraftBukkit - update CraftEnchant.getName(i) if this changes
    private static final net.minecraft.server.Enchantment[] byId = new net.minecraft.server.Enchantment[256];
    public static final net.minecraft.server.Enchantment[] b;
    private static final Map<MinecraftKey, net.minecraft.server.Enchantment> E = Maps.newHashMap();
    public static final net.minecraft.server.Enchantment PROTECTION_ENVIRONMENTAL = new EnchantmentProtection(0, new MinecraftKey("protection"), 10, 0);
    public static final net.minecraft.server.Enchantment PROTECTION_FIRE = new EnchantmentProtection(1, new MinecraftKey("fire_protection"), 5, 1);
    public static final net.minecraft.server.Enchantment PROTECTION_FALL = new EnchantmentProtection(2, new MinecraftKey("feather_falling"), 5, 2);
    public static final net.minecraft.server.Enchantment PROTECTION_EXPLOSIONS = new EnchantmentProtection(3, new MinecraftKey("blast_protection"), 2, 3);
    public static final net.minecraft.server.Enchantment PROTECTION_PROJECTILE = new EnchantmentProtection(4, new MinecraftKey("projectile_protection"), 5, 4);
    public static final net.minecraft.server.Enchantment OXYGEN = new EnchantmentOxygen(5, new MinecraftKey("respiration"), 2);
    public static final net.minecraft.server.Enchantment WATER_WORKER = new EnchantmentWaterWorker(6, new MinecraftKey("aqua_affinity"), 2);
    public static final net.minecraft.server.Enchantment THORNS = new EnchantmentThorns(7, new MinecraftKey("thorns"), 1);
    public static final net.minecraft.server.Enchantment DEPTH_STRIDER = new EnchantmentDepthStrider(8, new MinecraftKey("depth_strider"), 2);
    public static final net.minecraft.server.Enchantment DAMAGE_ALL = new EnchantmentWeaponDamage(16, new MinecraftKey("sharpness"), 10, 0);
    public static final net.minecraft.server.Enchantment DAMAGE_UNDEAD = new EnchantmentWeaponDamage(17, new MinecraftKey("smite"), 5, 1);
    public static final net.minecraft.server.Enchantment DAMAGE_ARTHROPODS = new EnchantmentWeaponDamage(18, new MinecraftKey("bane_of_arthropods"), 5, 2);
    public static final net.minecraft.server.Enchantment KNOCKBACK = new EnchantmentKnockback(19, new MinecraftKey("knockback"), 5);
    public static final net.minecraft.server.Enchantment FIRE_ASPECT = new EnchantmentFire(20, new MinecraftKey("fire_aspect"), 2);
    public static final net.minecraft.server.Enchantment LOOT_BONUS_MOBS = new EnchantmentLootBonus(21, new MinecraftKey("looting"), 2, EnchantmentSlotType.WEAPON);
    public static final net.minecraft.server.Enchantment DIG_SPEED = new EnchantmentDigging(32, new MinecraftKey("efficiency"), 10);
    public static final net.minecraft.server.Enchantment SILK_TOUCH = new EnchantmentSilkTouch(33, new MinecraftKey("silk_touch"), 1);
    public static final net.minecraft.server.Enchantment DURABILITY = new EnchantmentDurability(34, new MinecraftKey("unbreaking"), 5);
    public static final net.minecraft.server.Enchantment LOOT_BONUS_BLOCKS = new EnchantmentLootBonus(35, new MinecraftKey("fortune"), 2, EnchantmentSlotType.DIGGER);
    public static final net.minecraft.server.Enchantment ARROW_DAMAGE = new EnchantmentArrowDamage(48, new MinecraftKey("power"), 10);
    public static final net.minecraft.server.Enchantment ARROW_KNOCKBACK = new EnchantmentArrowKnockback(49, new MinecraftKey("punch"), 2);
    public static final net.minecraft.server.Enchantment ARROW_FIRE = new EnchantmentFlameArrows(50, new MinecraftKey("flame"), 2);
    public static final net.minecraft.server.Enchantment ARROW_INFINITE = new EnchantmentInfiniteArrows(51, new MinecraftKey("infinity"), 1);
    public static final net.minecraft.server.Enchantment LUCK = new EnchantmentLootBonus(61, new MinecraftKey("luck_of_the_sea"), 2, EnchantmentSlotType.FISHING_ROD);
    public static final net.minecraft.server.Enchantment LURE = new EnchantmentLure(62, new MinecraftKey("lure"), 2, EnchantmentSlotType.FISHING_ROD);
    public final int id;
    private final int weight;
    public EnchantmentSlotType slot;
    protected String name;

    public static net.minecraft.server.Enchantment getById(int i) {
        return i >= 0 && i < byId.length ? byId[i] : null;
    }

    protected Enchantment(int i, MinecraftKey minecraftkey, int j, EnchantmentSlotType enchantmentslottype) {
        this.id = i;
        this.weight = j;
        this.slot = enchantmentslottype;
        if (byId[i] != null) {
            throw new IllegalArgumentException("Duplicate enchantment id!");
        } else {
            byId[i] = this;
            E.put(minecraftkey, this);
        }

        org.bukkit.enchantments.Enchantment.registerEnchantment(new org.bukkit.craftbukkit.enchantments.CraftEnchantment(this)); // CraftBukkit
    }

    public static net.minecraft.server.Enchantment getByName(String s) {
        return (net.minecraft.server.Enchantment) E.get(new MinecraftKey(s));
    }

    public static Set<MinecraftKey> getEffects() {
        return E.keySet();
    }

    public int getRandomWeight() {
        return this.weight;
    }

    public int getStartLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int a(int i) {
        return 1 + i * 10;
    }

    public int b(int i) {
        return this.a(i) + 5;
    }

    public int a(int i, DamageSource damagesource) {
        return 0;
    }

    public float a(int i, EnumMonsterType enummonstertype) {
        return 0.0F;
    }

    public boolean a(net.minecraft.server.Enchantment enchantment) {
        return this != enchantment;
    }

    public net.minecraft.server.Enchantment c(String s) {
        this.name = s;
        return this;
    }

    public String a() {
        return "enchantment." + this.name;
    }

    public String d(int i) {
        String s = LocaleI18n.get(this.a());

        return s + " " + LocaleI18n.get("enchantment.level." + i);
    }

    public boolean canEnchant(ItemStack itemstack) {
        return this.slot.canEnchant(itemstack.getItem());
    }

    public void a(EntityLiving entityliving, Entity entity, int i) {}

    public void b(EntityLiving entityliving, Entity entity, int i) {}

    static {
        ArrayList arraylist = Lists.newArrayList();
        net.minecraft.server.Enchantment[] aenchantment = byId;
        int i = aenchantment.length;

        for (int j = 0; j < i; ++j) {
            net.minecraft.server.Enchantment enchantment = aenchantment[j];

            if (enchantment != null) {
                arraylist.add(enchantment);
            }
        }

        b = (net.minecraft.server.Enchantment[]) arraylist.toArray(new net.minecraft.server.Enchantment[arraylist.size()]);
    }
}
