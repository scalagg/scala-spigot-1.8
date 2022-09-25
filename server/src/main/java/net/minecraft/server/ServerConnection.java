package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.velocitypowered.natives.util.Natives;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerConnection {

    public enum EventGroupType {
        EPOLL,
        KQUEUE,
        NIO,
        DEFAULT
    }

    private static final WriteBufferWaterMark SERVER_WRITE_MARK = new WriteBufferWaterMark(1 << 20, 1 << 21);

    private static final Logger e = MinecraftServer.LOGGER;
    public static final LazyInitVar<NioEventLoopGroup> a = new LazyInitVar() {
        private NioEventLoopGroup a() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
        }

        protected Object init() {
            return this.a();
        }
    };
    public static final LazyInitVar<EpollEventLoopGroup> b = new LazyInitVar() {
        private EpollEventLoopGroup a() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
        }

        protected Object init() {
            return this.a();
        }
    };
    public static final LazyInitVar<LocalEventLoopGroup> c = new LazyInitVar() {
        private LocalEventLoopGroup a() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
        }

        protected Object init() {
            return this.a();
        }
    };
    private final MinecraftServer server;
    public volatile boolean started;
    private final List<ChannelFuture> listeningChannels = Collections.synchronizedList(Lists.newArrayList());
    private final List<NetworkManager> h = Collections.synchronizedList(Lists.newArrayList());

    // Paper start - prevent blocking on adding a new network manager while the server is ticking
    private final List<NetworkManager> pending = Collections.synchronizedList(Lists.newArrayList());

    private void addPending() {
        synchronized (pending) {
            this.h.addAll(pending); // Paper - OBFHELPER - List of network managers
            pending.clear();
        }
    }
    // Paper end

    private final EventGroupType eventGroupType;

    public ServerConnection(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
        this.started = true;

        if (server.ai()) /* use-native-transport */ {
            if (Epoll.isAvailable()) {
                this.eventGroupType = EventGroupType.EPOLL;
                return;
            } else if (KQueue.isAvailable()) {
                this.eventGroupType = EventGroupType.KQUEUE;
                return;
            }
        }

        this.eventGroupType = server.getTransport();
    }

    private static final boolean DISABLE_FLUSH_CONSOLIDATION = Boolean.getBoolean("Paper.disableFlushConsolidate"); // PandaSpigot

    public void a(InetAddress inetaddress, int i) throws IOException {
        bind(new java.net.InetSocketAddress(inetaddress, i));
    }

    public static EventLoopGroup boss, worker;

    public void bind(java.net.SocketAddress address) throws IOException {
        // PandaSpigot end
        synchronized (this.listeningChannels) {
            Class<? extends ServerChannel> channel;
            final int workerThreadCount = Runtime.getRuntime().availableProcessors();

            switch (eventGroupType) {
                default:
                case DEFAULT: {
                    MinecraftServer.LOGGER.info("[binder] Finding best event group type using fall-through.");
                }
                case EPOLL: {
                    if (Epoll.isAvailable()) {
                        boss = new EpollEventLoopGroup(0);
                        worker = new EpollEventLoopGroup(workerThreadCount);

                        channel = EpollServerSocketChannel.class;

                        MinecraftServer.LOGGER.info("[binder] Using Epoll, best of fall-through.");

                        break;
                    }
                }
                case KQUEUE: {
                    if (KQueue.isAvailable()) {
                        boss = new KQueueEventLoopGroup(0);
                        worker = new KQueueEventLoopGroup(workerThreadCount);

                        channel = KQueueServerSocketChannel.class;

                        MinecraftServer.LOGGER.info("[binder] Using KQueue, second-best of fall-through.");
                        break;
                    }
                }
                case NIO: {
                    boss = new NioEventLoopGroup(0);
                    worker = new NioEventLoopGroup(workerThreadCount);

                    channel = NioServerSocketChannel.class;

                    MinecraftServer.LOGGER.info("[binder] Using NIO, last element of fall-through.");

                    break;
                }
            }

            MinecraftServer.LOGGER.info("[natives] Using " + Natives.compress.getLoadedVariant() + " compression from Velocity.");
            MinecraftServer.LOGGER.info("[natives] Using " + Natives.cipher.getLoadedVariant() + " cipher from Velocity.");

            this.listeningChannels.add(
                ((new ServerBootstrap().channel(channel))
                    .childHandler(new ChannelInitializer<Channel>() {
                        protected void initChannel(@NotNull Channel channel) {
                            try {
                                channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                                channel.config().setOption(ChannelOption.IP_TOS, 0x18);
                            } catch (ChannelException channelexception) {
                                // Ignore
                            }

                            if (!DISABLE_FLUSH_CONSOLIDATION) {
                                channel.pipeline().addFirst("flush_consolidator", new FlushConsolidationHandler()); // PandaSpigot
                            }

                            channel
                                .pipeline()
                                .addLast("timeout", new ReadTimeoutHandler(30))
                                .addLast("legacy_query", new LegacyPingHandler(net.minecraft.server.ServerConnection.this))
                                .addLast("splitter", new PacketSplitter())
                                .addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND))
                                .addLast("prepender", new PacketPrepender())
                                .addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));

                            final NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);
                            pending.add(networkmanager); // Paper

                            channel.pipeline().addLast("packet_handler", networkmanager);

                            networkmanager.isPending = false; // PandaSpigot
                            networkmanager.setPacketListener(
                                new HandshakeListener(net.minecraft.server.ServerConnection.this.server, networkmanager)
                            );
                        }
                    })
                    .group(boss, worker)
                    .localAddress(address))
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, SERVER_WRITE_MARK)
                    .bind()
                    .syncUninterruptibly()
            ); // PandaSpigot - Unix domain socket support
        }
    }

    public void b() {
        this.started = false;

        for (final ChannelFuture future : this.listeningChannels) {
            try {
                future.channel().close().sync();
            } catch (InterruptedException interruptedexception) {
                e.error("Interrupted whilst closing channel");
            }
        }
    }

    public void c() {
        synchronized (this.h) {
            addPending(); // Paper
            Iterator iterator = this.h.iterator();

            while (iterator.hasNext()) {
                final NetworkManager networkmanager = (NetworkManager) iterator.next();

                if (!networkmanager.h()) {
                    if (!networkmanager.g()) {
                        if (networkmanager.preparing) {
                            continue;
                        }

                        iterator.remove();
                        networkmanager.l();
                    } else {
                        try {
                            networkmanager.a();
                        } catch (Exception exception) {
                            if (networkmanager.c()) {
                                CrashReport crashreport = CrashReport.a(exception, "Ticking memory connection");
                                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Ticking connection");

                                crashreportsystemdetails.a("Connection", new Callable() {
                                    public String a() throws Exception {
                                        return networkmanager.toString();
                                    }

                                    public Object call() throws Exception {
                                        return this.a();
                                    }
                                });
                                throw new ReportedException(crashreport);
                            }

                            e.warn("Failed to handle packet for " + networkmanager.getSocketAddress(), exception);
                            final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");

                            networkmanager.a(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener() {
                                public void operationComplete(Future future) throws Exception {
                                    networkmanager.close(chatcomponenttext);
                                }
                            }, new GenericFutureListener[0]);
                            networkmanager.k();
                        }
                    }
                }
            }
        }
    }

    public MinecraftServer d() {
        return this.server;
    }

}
