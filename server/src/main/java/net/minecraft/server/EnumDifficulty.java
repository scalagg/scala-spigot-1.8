//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.server;

public enum EnumDifficulty {
    PEACEFUL(0, "options.difficulty.peaceful"),
    EASY(1, "options.difficulty.easy"),
    NORMAL(2, "options.difficulty.normal"),
    HARD(3, "options.difficulty.hard");

    private static final EnumDifficulty[] e = new EnumDifficulty[values().length];
    private final int f;
    private final String g;

    private EnumDifficulty(int var3, String var4) {
        this.f = var3;
        this.g = var4;
    }

    public int a() {
        return this.f;
    }

    public static EnumDifficulty getById(int var0) {
        return e[var0 % e.length];
    }

    public String b() {
        return this.g;
    }

    static {
        EnumDifficulty[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            EnumDifficulty var3 = var0[var2];
            e[var3.f] = var3;
        }

    }
}
