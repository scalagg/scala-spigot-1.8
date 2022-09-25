//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.server;

public class IntHashMap<V> {
    private transient IntHashMapEntry<V>[] a = new IntHashMapEntry[16];
    private transient int b;
    private int c = 12;
    private final float d = 0.75F;

    public IntHashMap() {
    }

    private static int g(int var0) {
        var0 ^= var0 >>> 20 ^ var0 >>> 12;
        return var0 ^ var0 >>> 7 ^ var0 >>> 4;
    }

    private static int a(int var0, int var1) {
        return var0 & var1 - 1;
    }

    public V get(int var1) {
        int var2 = g(var1);

        for(IntHashMapEntry var3 = this.a[a(var2, this.a.length)]; var3 != null; var3 = var3.c) {
            if (var3.a == var1) {
                return (V) var3.b;
            }
        }

        return null;
    }

    public boolean b(int var1) {
        return this.c(var1) != null;
    }

    final IntHashMapEntry<V> c(int var1) {
        int var2 = g(var1);

        for(IntHashMapEntry var3 = this.a[a(var2, this.a.length)]; var3 != null; var3 = var3.c) {
            if (var3.a == var1) {
                return var3;
            }
        }

        return null;
    }

    public void a(int var1, V var2) {
        int var3 = g(var1);
        int var4 = a(var3, this.a.length);

        for(IntHashMapEntry var5 = this.a[var4]; var5 != null; var5 = var5.c) {
            if (var5.a == var1) {
                var5.b = var2;
                return;
            }
        }

        this.a(var3, var1, var2, var4);
    }

    private void h(int var1) {
        IntHashMapEntry[] var2 = this.a;
        int var3 = var2.length;
        if (var3 == 1073741824) {
            this.c = Integer.MAX_VALUE;
        } else {
            IntHashMapEntry[] var4 = new IntHashMapEntry[var1];
            this.a(var4);
            this.a = var4;
            this.c = (int)((float)var1 * this.d);
        }
    }

    private void a(IntHashMapEntry<V>[] var1) {
        IntHashMapEntry[] var2 = this.a;
        int var3 = var1.length;

        for(int var4 = 0; var4 < var2.length; ++var4) {
            IntHashMapEntry var5 = var2[var4];
            if (var5 != null) {
                var2[var4] = null;

                IntHashMapEntry var6;
                do {
                    var6 = var5.c;
                    int var7 = a(var5.d, var3);
                    var5.c = var1[var7];
                    var1[var7] = var5;
                    var5 = var6;
                } while(var6 != null);
            }
        }

    }

    public V d(int var1) {
        IntHashMapEntry var2 = this.e(var1);
        return var2 == null ? null : (V) var2.b;
    }

    final IntHashMapEntry<V> e(int var1) {
        int var2 = g(var1);
        int var3 = a(var2, this.a.length);
        IntHashMapEntry var4 = this.a[var3];

        IntHashMapEntry var5;
        IntHashMapEntry var6;
        for(var5 = var4; var5 != null; var5 = var6) {
            var6 = var5.c;
            if (var5.a == var1) {
                --this.b;
                if (var4 == var5) {
                    this.a[var3] = var6;
                } else {
                    var4.c = var6;
                }

                return var5;
            }

            var4 = var5;
        }

        return var5;
    }

    public void c() {
        IntHashMapEntry[] var1 = this.a;

        for(int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = null;
        }

        this.b = 0;
    }

    private void a(int var1, int var2, V var3, int var4) {
        IntHashMapEntry var5 = this.a[var4];
        this.a[var4] = new IntHashMapEntry(var1, var2, var3, var5);
        if (this.b++ >= this.c) {
            this.h(2 * this.a.length);
        }

    }

    static class IntHashMapEntry<V> {
        final int a;
        V b;
        IntHashMapEntry<V> c;
        final int d;

        IntHashMapEntry(int var1, int var2, V var3, IntHashMapEntry<V> var4) {
            this.b = var3;
            this.c = var4;
            this.a = var2;
            this.d = var1;
        }

        public final int a() {
            return this.a;
        }

        public final V b() {
            return this.b;
        }

        public final boolean equals(Object var1) {
            if (!(var1 instanceof IntHashMapEntry)) {
                return false;
            } else {
                IntHashMapEntry var2 = (IntHashMapEntry)var1;
                Integer var3 = this.a();
                Integer var4 = var2.a();
                if (var3 == var4 || var3 != null && var3.equals(var4)) {
                    Object var5 = this.b();
                    Object var6 = var2.b();
                    if (var5 == var6 || var5 != null && var5.equals(var6)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public final int hashCode() {
            return IntHashMap.g(this.a);
        }

        public final String toString() {
            return this.a() + "=" + this.b();
        }
    }
}
