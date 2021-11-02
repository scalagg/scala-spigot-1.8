package net.minecraft.server;

import net.minecraft.server.AttributeModifier;
import net.minecraft.server.MinecraftKey;
import net.minecraft.server.MobEffectList;

public class MobEffectAttackDamage extends MobEffectList {

    protected MobEffectAttackDamage(int i, MinecraftKey minecraftkey, boolean flag, int j) {
        super(i, minecraftkey, flag, j);
    }

    public double a(int i, AttributeModifier attributemodifier) {
        // PaperSpigot - Configurable modifiers for strength and weakness effects
        return this.id == MobEffectList.WEAKNESS.id ? (double) (org.github.paperspigot.PaperSpigotConfig.weaknessEffectModifier * (float) (i + 1)) : org.github.paperspigot.PaperSpigotConfig.strengthEffectModifier * (double) (i + 1);
    }
}
