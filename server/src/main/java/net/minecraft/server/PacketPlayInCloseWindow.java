package net.minecraft.server;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketListenerPlayIn;

import java.io.IOException;

public class PacketPlayInCloseWindow implements Packet<PacketListenerPlayIn> {

    private int id;

    public PacketPlayInCloseWindow() {}

    // CraftBukkit start
    public PacketPlayInCloseWindow(int id) {
        this.id = id;
    }
    // CraftBukkit end

    public void a(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.a(this);
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.id = packetdataserializer.readByte();
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.writeByte(this.id);
    }
}
