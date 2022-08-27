package net.minecraft.server;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.Queue;

public class NetworkManager extends SimpleChannelInboundHandler<Packet> {

    private static final Logger g = MinecraftServer.LOGGER;
    public static final Marker a = MarkerManager.getMarker("NETWORK");
    public static final Marker b = MarkerManager.getMarker("NETWORK_PACKETS", a);
    public static final AttributeKey<EnumProtocol> c = AttributeKey.valueOf("protocol");

    public static final LazyInitVar<NioEventLoopGroup> d = new LazyInitVar<NioEventLoopGroup>() {
        @Override
        protected NioEventLoopGroup init() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyInitVar<EpollEventLoopGroup> e = new LazyInitVar<EpollEventLoopGroup>() {
        @Override
        protected EpollEventLoopGroup init() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyInitVar<LocalEventLoopGroup> f = new LazyInitVar<LocalEventLoopGroup>() {
        @Override
        protected LocalEventLoopGroup init() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
        }
    };

    private final EnumProtocolDirection h;
    private final Queue<net.minecraft.server.NetworkManager.QueuedPacket> i = Queues.newConcurrentLinkedQueue();
    //    private final ReentrantReadWriteLock j = new ReentrantReadWriteLock();
    public Channel channel;
    // Spigot Start // PAIL
    public SocketAddress l;
    public java.util.UUID spoofedUUID;
    public com.mojang.authlib.properties.Property[] spoofedProfile;
    public boolean preparing = true;
    // Spigot End
    private PacketListener m;
    private IChatBaseComponent n;
    private boolean o;
    private boolean p;

    private boolean openedBook;

    // PandaSpigot start - Optimize network
    public boolean isPending = true;
    public boolean queueImmunity = false;
    public EnumProtocol protocol;
    // PandaSpigot end

    // PandaSpigot start - allow controlled flushing
    volatile boolean canFlush = true;
    private final java.util.concurrent.atomic.AtomicInteger packetWrites = new java.util.concurrent.atomic.AtomicInteger();
    private int flushPacketsStart;
    private final Object flushLock = new Object();

    public void disableAutomaticFlush() {
        synchronized (this.flushLock) {
            this.flushPacketsStart = this.packetWrites.get(); // must be volatile and before canFlush = false
            this.canFlush = false;
        }
    }

    public void enableAutomaticFlush() {
        synchronized (this.flushLock) {
            this.canFlush = true;
            if (this.packetWrites.get() != this.flushPacketsStart) { // must be after canFlush = true
                this.flush(); // only make the flush call if we need to
            }
        }
    }

    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> {
                this.channel.flush();
            });
        }
    }
    // PandaSpigot end


    private static final boolean ENABLE_EXPLICIT_FLUSH = Boolean.getBoolean("paper.explicit-flush"); // PandaSpigot - Disable explicit flushing

    public static Channel getChannel(final net.minecraft.server.NetworkManager nm) {
        return nm.channel;
    }

    public NetworkManager(EnumProtocolDirection enumprotocoldirection) {
        this.h = enumprotocoldirection;
    }

    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
        super.channelActive(channelhandlercontext);
        this.channel = channelhandlercontext.channel();
        this.l = this.channel.remoteAddress();
        // Spigot Start
        this.preparing = false;
        // Spigot End

        try {
            this.a(EnumProtocol.HANDSHAKING);
        } catch (Throwable throwable) {
            g.fatal(throwable);
        }

    }

    public void a(EnumProtocol enumprotocol) {
        this.protocol = enumprotocol; // PandaSpigot
        this.channel.attr(c).set(enumprotocol);
        this.channel.config().setAutoRead(true);
        g.debug("Enabled auto read");
    }

    public void channelInactive(ChannelHandlerContext channelhandlercontext) throws Exception {
        this.close(new ChatMessage("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) throws Exception {
        ChatMessage chatmessage;

        if (throwable instanceof TimeoutException) {
            chatmessage = new ChatMessage("disconnect.timeout");
        } else {
            chatmessage = new ChatMessage("disconnect.genericReason", "Internal Exception: " + throwable);
        }

        this.close(chatmessage);
        if (MinecraftServer.getServer().isDebugging()) throwable.printStackTrace(); // Spigot
    }

    protected void a(ChannelHandlerContext channelhandlercontext, Packet packet) throws Exception {
        if (this.channel.isOpen()) {
            if (packet instanceof PacketPlayInCustomPayload) {
                PacketPlayInCustomPayload payload = (PacketPlayInCustomPayload) packet;
                String name = payload.a();

                if (name.equalsIgnoreCase("MC|BSign") || name.equalsIgnoreCase("MC|BEdit")) {
                    if (this.m instanceof PlayerConnection) {
                        byte[] data = payload.b().array();

                        if (data.length > 15000) {
                            this.close(new ChatMessage("Invalid book packet"));
                            return;
                        }
                        if (!this.openedBook) {
                            this.close(new ChatMessage("Invalid book packet"));
                            return;
                        }
                        this.openedBook = false;
                    }
                }
            } else if (packet instanceof PacketPlayInBlockPlace) {
                ItemStack stack = ((PacketPlayInBlockPlace) packet).getItemStack();
                if (stack != null && stack.getItem() != null && stack.getItem().getName().equalsIgnoreCase("item.writingBook")) {
                    this.openedBook = true;
                }
            }
            try {
                packet.a(this.m);
            } catch (CancelledPacketHandleException cancelledpackethandleexception) {
            }
        }

    }

    public void a(PacketListener packetlistener) {
        Validate.notNull(packetlistener, "packetListener");
        g.debug("Set listener of {} to {}", this, packetlistener);
        this.m = packetlistener;
    }

    // PandaSpigot start
    public EntityPlayer getPlayer() {
        if (this.m instanceof PlayerConnection) {
            return ((PlayerConnection) this.m).player;
        } else {
            return null;
        }
    }

    private static class InnerUtil { // Attempt to hide these methods from ProtocolLib, so it doesn't accidentally pick them up.
        private static java.util.List<Packet> buildExtraPackets(Packet packet) {
            java.util.List<Packet> extra = packet.getExtraPackets();
            if (extra == null || extra.isEmpty()) {
                return null;
            }
            java.util.List<Packet> ret = new java.util.ArrayList<>(1 + extra.size());
            buildExtraPackets0(extra, ret);
            return ret;
        }

        private static void buildExtraPackets0(java.util.List<Packet> extraPackets, java.util.List<Packet> into) {
            for (Packet extra : extraPackets) {
                into.add(extra);
                java.util.List<Packet> extraExtra = extra.getExtraPackets();
                if (extraExtra != null && !extraExtra.isEmpty()) {
                    buildExtraPackets0(extraExtra, into);
                }
            }
        }

        private static boolean canSendImmediate(NetworkManager networkManager, Packet<?> packet) {
            return networkManager.isPending || networkManager.protocol != EnumProtocol.PLAY ||
                packet instanceof PacketPlayOutKeepAlive ||
                packet instanceof PacketPlayOutChat ||
                packet instanceof PacketPlayOutTabComplete ||
                packet instanceof PacketPlayOutTitle;
        }
    }
    // PandaSpigot end

    public void handle(Packet packet) {
        // PandaSpigot start
        this.a(packet, null, null);
        // PandaSpigot end
    }

    public void a(Packet packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener, GenericFutureListener<? extends Future<? super Void>>[] agenericfuturelistener) {
        GenericFutureListener<? extends Future<? super Void>>[] listeners = null;
        if (genericfuturelistener != null || agenericfuturelistener != null) { // cannot call ArrayUtils.add with both null arguments
            listeners = ArrayUtils.add(agenericfuturelistener, 0, genericfuturelistener);
        }

        boolean connected = this.isConnected();
        if (!connected && !this.preparing) {
            return; // Do nothing
        }
        packet.onPacketDispatch(getPlayer());
        if (connected && (InnerUtil.canSendImmediate(this, packet) || (
            MinecraftServer.getServer().isMainThread() && packet.isReady() && this.i.isEmpty() &&
                (packet.getExtraPackets() == null || packet.getExtraPackets().isEmpty())
        ))) {
            this.dispatchPacket(packet, listeners);
            return;
        }
        // write the packets to the queue, then flush - antixray hooks there already
        java.util.List<Packet> extraPackets = InnerUtil.buildExtraPackets(packet);
        boolean hasExtraPackets = extraPackets != null && !extraPackets.isEmpty();
        if (!hasExtraPackets) {
            this.i.add(new NetworkManager.QueuedPacket(packet, listeners));
        } else {
            java.util.List<NetworkManager.QueuedPacket> packets = new java.util.ArrayList<>(1 + extraPackets.size());
            packets.add(new NetworkManager.QueuedPacket(packet, (GenericFutureListener<? extends Future<? super Void>>[]) null)); // delay the future listener until the end of the extra packets

            for (int i = 0, len = extraPackets.size(); i < len; ) {
                Packet extra = extraPackets.get(i);
                boolean end = ++i == len;
                packets.add(new NetworkManager.QueuedPacket(extra, end ? listeners : null)); // append listener to the end
            }

            this.i.addAll(packets); // atomic
        }

        this.sendPacketQueue();
    }

    // PandaSpigot - OBFHELPER
    private void dispatchPacket(Packet<?> packet, final GenericFutureListener<? extends Future<? super Void>>[] agenericfuturelistener) {
        this.a(packet, agenericfuturelistener);
    }
    // PandaSpigot end

    private void a(Packet packet, GenericFutureListener<? extends Future<? super Void>>[] listeners) {
        // PandaSpigot start - add flush parameter
        this.writePacket(packet, listeners, Boolean.TRUE);
    }

    private void writePacket(Packet packet, final GenericFutureListener<? extends Future<? super Void>>[] listeners, Boolean flushConditional) {
        this.packetWrites.getAndIncrement(); // must be before using canFlush
        boolean effectiveFlush = flushConditional == null ? this.canFlush : flushConditional;
        final boolean flush = effectiveFlush || packet instanceof PacketPlayOutKeepAlive || packet instanceof PacketPlayOutKickDisconnect; // no delay for certain packets
        // PandaSpigot end - add flush parameter
        final EnumProtocol packetProtocol = EnumProtocol.a(packet);
        final EnumProtocol channelProtocol = this.channel.attr(c).get();

        if (channelProtocol != packetProtocol) {
            g.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        EntityPlayer player = getPlayer(); // PandaSpigot
        if (this.channel.eventLoop().inEventLoop()) {
            if (packetProtocol != channelProtocol) {
                this.a(packetProtocol);
            }

            // PandaSpigot start
            if (!isConnected()) {
                packet.onPacketDispatchFinish(player, null);
                return;
            }

            try {
                // PandaSpigot end

                ChannelFuture channelfuture = (flush) ? this.channel.writeAndFlush(packet) : this.channel.write(packet); // PandaSpigot - add flush parameter

                if (listeners != null) {
                    channelfuture.addListeners(listeners);
                }

                // PandaSpigot start
                if (packet.hasFinishListener()) {
                    channelfuture.addListener((ChannelFutureListener) channelFuture -> packet.onPacketDispatchFinish(player, channelFuture));
                }
                // PandaSpigot end

                channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                // PandaSpigot start
            } catch (Exception e) {
                g.error("NetworkException: " + player, e);
                close(new ChatMessage("disconnect.genericReason", "Internal Exception: " + e.getMessage()));
                packet.onPacketDispatchFinish(player, null);
            }
            // PandaSpigot end
        } else {
            Runnable command = new Runnable() { // PandaSpigot - optimize packets that are not flushed
                public void run() {
                    if (packetProtocol != channelProtocol) {
                        net.minecraft.server.NetworkManager.this.a(packetProtocol);
                    }

                    // PandaSpigot start
                    if (!isConnected()) {
                        packet.onPacketDispatchFinish(player, null);
                        return;
                    }
                    try {
                        // PandaSpigot end
                        ChannelFuture channelfuture = (flush) ? NetworkManager.this.channel.writeAndFlush(packet) : NetworkManager.this.channel.write(packet); // PandaSpigot - add flush parameter

                        if (listeners != null) {
                            channelfuture.addListeners(listeners);
                        }

                        channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                        // PandaSpigot start
                    } catch (Exception e) {
                        g.error("NetworkException: " + player, e);
                        close(new ChatMessage("disconnect.genericReason", "Internal Exception: " + e.getMessage()));
                        packet.onPacketDispatchFinish(player, null);
                    }
                    // PandaSpigot end
                }
                // PandaSpigot start - optimize packets that are not flushed
            };

            if (!flush) {
                // create a LazyRunnable that when executed, calls command.run()
                io.netty.util.concurrent.AbstractEventExecutor.LazyRunnable run = command::run;
                this.channel.eventLoop().execute(run);
            } else {
                // if flushing, just schedule like normal
                this.channel.eventLoop().execute(command);
            }
            // PandaSpigot end
        }

    }

    // PandaSpigot start - rewrite this to be safer if ran off main thread
    private boolean sendPacketQueue() {
        return this.m();
    } // PandaSpigot - OBFHELPER // void -> boolean

    private boolean m() {
        if (!this.isConnected()) {
            return true;
        }
        if (MinecraftServer.getServer().isMainThread()) {
            return this.processQueue();
        } else if (this.isPending) {
            // Should only happen during login/status stages
            synchronized (this.i) {
                return this.processQueue();
            }
        }
        return false;
    }

    private boolean processQueue() {
        if (this.i.isEmpty()) return true;

        // PandaSpigot start - make only one flush call per sendPacketQueue() call
        final boolean needsFlush = this.canFlush; // make only one flush call per sendPacketQueue() call
        boolean hasWrotePacket = false;
        // PandaSpigot end

        // If we are on main, we are safe here in that nothing else should be processing queue off main anymore
        // But if we are not on main due to login/status, the parent is synchronized on packetQueue
        java.util.Iterator<QueuedPacket> iterator = this.i.iterator();
        while (iterator.hasNext()) {
            NetworkManager.QueuedPacket queued = iterator.next(); // poll -> peek

            // Fix NPE (Spigot bug caused by handleDisconnection())
            if (queued == null) return true;

            Packet<?> packet = queued.getPacket();
            if (!packet.isReady()) {
                // PandaSpigot start - make only one flush call per sendPacketQueue() call
                if (hasWrotePacket && (needsFlush || this.canFlush)) {
                    this.flush();
                }
                // PandaSpigot end
                return false;
            } else {
                iterator.remove();
                // PandaSpigot start - make only one flush call per sendPacketQueue() call
                this.writePacket(packet, queued.getGenericFutureListeners(), (!iterator.hasNext() && (needsFlush || this.canFlush)) ? Boolean.TRUE : Boolean.FALSE);
                hasWrotePacket = true;
                // PandaSpigot end
            }
        }

        return true;
    }
    // PandaSpigot end

    public void a() {
        this.m();

        if (this.m instanceof IUpdatePlayerListBox) {
            ((IUpdatePlayerListBox) this.m).c();
        }

        if (ENABLE_EXPLICIT_FLUSH) {
            this.channel.eventLoop().execute(() -> this.channel.flush()); // PandaSpigot - we don't need to explicit flush here, but allow opt-in in case issues are found to a better version
        }
    }

    public SocketAddress getSocketAddress() {
        return this.l;
    }

    // PandaSpigot start
    public void clearPacketQueue() {
        EntityPlayer player = getPlayer();
        this.i.forEach(queuedPacket -> {
            Packet<?> packet = queuedPacket.getPacket();
            if (packet.hasFinishListener()) {
                packet.onPacketDispatchFinish(player, null);
            }
        });
        this.i.clear();
    }
    // PandaSpigot end

    public void close(IChatBaseComponent ichatbasecomponent) {
        this.preparing = false;
        this.clearPacketQueue(); // PandaSpigot

        if (this.channel.isOpen()) {
            this.channel.close();
            this.n = ichatbasecomponent;
        }

    }

    public boolean isConnected() {
        return this.g();
    } // PandaSpigot - OBFHELPER

    public boolean c() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public void a(SecretKey secretkey) {
        this.o = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new PacketDecrypter(MinecraftEncryption.a(2, secretkey)));
        this.channel.pipeline().addBefore("prepender", "encrypt", new PacketEncrypter(MinecraftEncryption.a(1, secretkey)));
    }

    public boolean g() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean h() {
        return this.channel == null;
    }

    public PacketListener getPacketListener() {
        return this.m;
    }

    public IChatBaseComponent j() {
        return this.n;
    }

    public void k() {
        this.channel.config().setAutoRead(false);
    }

    public void a(int i) {
        if (i >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof PacketDecompressor) {
                ((PacketDecompressor) this.channel.pipeline().get("decompress")).a(i);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new PacketDecompressor(i));
            }

            if (this.channel.pipeline().get("compress") instanceof PacketCompressor) {
                ((PacketCompressor) this.channel.pipeline().get("decompress")).a(i);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress", new PacketCompressor(i));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof PacketDecompressor) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof PacketCompressor) {
                this.channel.pipeline().remove("compress");
            }
        }

    }

    public void l() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (!this.p) {
                this.p = true;
                if (this.j() != null) {
                    this.getPacketListener().a(this.j());
                } else if (this.getPacketListener() != null) {
                    this.getPacketListener().a(new ChatComponentText("Disconnected"));
                }
                this.clearPacketQueue(); // Free up packet queue. // PandaSpigot
            }
        }
    }

    public EnumProtocol getProtocol() {
        return this.channel.attr(c).get();
    }

    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet object) throws Exception {
        if (g()) {
            this.a(channelhandlercontext, object);
        }
    }

    static class QueuedPacket {

        private final Packet a;
        private final GenericFutureListener<? extends Future<? super Void>>[] b;

        public QueuedPacket(Packet packet, GenericFutureListener<? extends Future<? super Void>>... agenericfuturelistener) {
            this.a = packet;
            this.b = agenericfuturelistener;
        }

        public Packet getPacket() {
            return this.a;
        } // PandaSpigot - OBFHELPER

        public GenericFutureListener<? extends Future<? super Void>>[] getGenericFutureListeners() {
            return this.b;
        } // PandaSpigot - OBFHELPER
    }

    public SocketAddress getRawAddress() {
        return this.channel.remoteAddress();
    }

}
