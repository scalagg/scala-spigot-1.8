package net.minecraft.server;

import gg.scala.spigot.network.VarIntUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable // PandaSpigot
public class PacketPrepender extends MessageToByteEncoder<ByteBuf> {
    public static final PacketPrepender INSTANCE = new PacketPrepender();

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        // PandaSpigot start
        VarIntUtil.writeVarInt(out, msg.readableBytes());
        out.writeBytes(msg);
        // PandaSpigot end
    }

    // PandaSpigot start
    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
        int anticipatedRequiredCapacity = VarIntUtil.varIntBytes(msg.readableBytes())
            + msg.readableBytes();

        return ctx.alloc().directBuffer(anticipatedRequiredCapacity);
    }
    // PandaSpigot end
}
