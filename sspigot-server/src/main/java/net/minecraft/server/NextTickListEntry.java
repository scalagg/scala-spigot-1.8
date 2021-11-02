package net.minecraft.server;

import net.minecraft.server.Block;
import net.minecraft.server.BlockPosition;

public class NextTickListEntry implements Comparable<net.minecraft.server.NextTickListEntry> {

    private static long d;
    private final Block e;
    public final BlockPosition a;
    public long b;
    public int c;
    private long f;

    public NextTickListEntry(BlockPosition blockposition, Block block) {
        this.f = (long) (d++);
        this.a = blockposition;
        this.e = block;
    }

    public boolean equals(Object object) {
        if (!(object instanceof net.minecraft.server.NextTickListEntry)) {
            return false;
        } else {
            net.minecraft.server.NextTickListEntry nextticklistentry = (net.minecraft.server.NextTickListEntry) object;

            return this.a.equals(nextticklistentry.a) && Block.a(this.e, nextticklistentry.e);
        }
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    public net.minecraft.server.NextTickListEntry a(long i) {
        this.b = i;
        return this;
    }

    public void a(int i) {
        this.c = i;
    }

    public int a(net.minecraft.server.NextTickListEntry nextticklistentry) {
        return this.b < nextticklistentry.b ? -1 : (this.b > nextticklistentry.b ? 1 : (this.c != nextticklistentry.c ? this.c - nextticklistentry.c : (this.f < nextticklistentry.f ? -1 : (this.f > nextticklistentry.f ? 1 : 0))));
    }

    public String toString() {
        return Block.getId(this.e) + ": " + this.a + ", " + this.b + ", " + this.c + ", " + this.f;
    }

    public Block a() {
        return this.e;
    }

    public int compareTo(net.minecraft.server.NextTickListEntry object) {
        return this.a((net.minecraft.server.NextTickListEntry) object);
    }
}
