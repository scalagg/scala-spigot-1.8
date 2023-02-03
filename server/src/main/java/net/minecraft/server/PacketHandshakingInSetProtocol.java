package net.minecraft.server;

import net.minecraft.server.EnumProtocol;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketDataSerializer;
import net.minecraft.server.PacketHandshakingInListener;

import java.io.IOException;

public class PacketHandshakingInSetProtocol implements Packet<PacketHandshakingInListener> {

    private int a;
    public String hostname;
    public int port;
    private EnumProtocol d;

    public PacketHandshakingInSetProtocol() {}

    @Override
    public void a(PacketDataSerializer serializer) throws IOException {
        this.a = serializer.readVarInt();
        this.hostname = serializer.c(Short.MAX_VALUE); // Spigot
        this.port = serializer.readUnsignedShort();
        this.d = EnumProtocol.isValidIntention(serializer.readVarInt());
    }

    @Override
    public void b(PacketDataSerializer serializer) throws IOException {
        serializer.b(this.a);
        serializer.a(this.hostname);
        serializer.writeShort(this.port);
        serializer.b(this.d.getStateId());
    }

    public void a(PacketHandshakingInListener packethandshakinginlistener) {
        packethandshakinginlistener.a(this);
    }

    public EnumProtocol a() {
        return this.d;
    }

    public int b() {
        return this.a;
    }
}
