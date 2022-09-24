package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger a = LogManager.getLogger();
    private static final Marker b;
    private final EnumProtocolDirection c;

    public PacketEncoder(EnumProtocolDirection var1) {
        this.c = var1;
    }

    // Paper start
    private static final int MAX_PACKET_SIZE = 2097152;

    public static class PacketTooLargeException extends RuntimeException {
        private final Packet<?> packet;
        PacketTooLargeException(Packet<?> packet, int packetLength) {
            super("PacketTooLarge - " + packet.getClass().getSimpleName() + " is " + packetLength +". Max is " + MAX_PACKET_SIZE);
            this.packet = packet;
        }

        public Packet<?> getPacket() {
            return packet;
        }
    }
    // Paper end

    protected void encode(ChannelHandlerContext context, Packet<?> packet, ByteBuf bytebuf) throws Exception {
        Integer var4 = ((EnumProtocol)context.channel().attr(NetworkManager.c).get()).a(this.c, packet);
        if (a.isDebugEnabled()) {
            a.debug(b, "OUT: [{}:{}] {}", new Object[]{context.channel().attr(NetworkManager.c).get(), var4, packet.getClass().getName()});
        }

        if (var4 == null) {
            throw new IOException("Can't serialize unregistered packet");
        } else {
            PacketDataSerializer serializer = new PacketDataSerializer(bytebuf);
            serializer.b(var4);

            try {
                if (packet instanceof PacketPlayOutNamedEntitySpawn) {
                    packet = packet;
                }

                packet.b(serializer);
            } catch (Throwable var7) {
                a.error(var7);
            }

            // Paper start
            int packetLength = bytebuf.readableBytes();
            if (packetLength > MAX_PACKET_SIZE) {
                throw new PacketTooLargeException(packet, packetLength);
            }
            // Paper end
        }


    }

    static {
        b = MarkerManager.getMarker("PACKET_SENT", NetworkManager.b);
    }
}
