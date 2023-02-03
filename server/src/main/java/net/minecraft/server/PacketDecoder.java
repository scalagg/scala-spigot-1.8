package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

	private final EnumProtocolDirection c;

	public PacketDecoder(EnumProtocolDirection var1) {
		this.c = var1;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (!in.isReadable()) {
			return;
		}

		PacketDataSerializer packetDataHelper = new PacketDataSerializer(in);
		int packetId = packetDataHelper.readVarInt();
		Packet<?> packet = ctx.channel().attr(NetworkManager.ATTRIBUTE_PROTOCOL).get().createPacket(this.c, packetId);
		if (packet == null) {
			throw new IOException("Bad packet id " + packetId);
		}

		packet.a(packetDataHelper);

		if (packetDataHelper.isReadable()) {
			throw new IOException("Packet " + ctx.channel().attr(NetworkManager.ATTRIBUTE_PROTOCOL).get().getStateId()
					+ "/" + packetId + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found "
					+ packetDataHelper.readableBytes() + " bytes extra whilst reading packet " + packetId);
		}
		out.add(packet);
	}
}