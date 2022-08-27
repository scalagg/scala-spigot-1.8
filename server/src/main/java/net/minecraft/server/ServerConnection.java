package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.CrashReport;
import net.minecraft.server.HandshakeListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerConnection {

    private static final Logger e = MinecraftServer.LOGGER;
    public static final LazyInitVar<NioEventLoopGroup> a = new LazyInitVar() {
        protected NioEventLoopGroup a() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
        }

        protected Object init() {
            return this.a();
        }
    };
    public static final LazyInitVar<EpollEventLoopGroup> b = new LazyInitVar() {
        protected EpollEventLoopGroup a() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
        }

        protected Object init() {
            return this.a();
        }
    };
    public static final LazyInitVar<LocalEventLoopGroup> c = new LazyInitVar() {
        protected LocalEventLoopGroup a() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
        }

        protected Object init() {
            return this.a();
        }
    };
    private final MinecraftServer f;
    public volatile boolean d;
    private final List<ChannelFuture> g = Collections.synchronizedList(Lists.<ChannelFuture>newArrayList());
    private final List<NetworkManager> h = Collections.synchronizedList(Lists.<NetworkManager>newArrayList());

    // Paper start - prevent blocking on adding a new network manager while the server is ticking
    private final List<NetworkManager> pending = Collections.synchronizedList(Lists.<NetworkManager>newArrayList());
    private void addPending() {
        synchronized (pending) {
            this.h.addAll(pending); // Paper - OBFHELPER - List of network managers
            pending.clear();
        }
    }
    // Paper end

    public ServerConnection(MinecraftServer minecraftserver) {
        this.f = minecraftserver;
        this.d = true;
    }


    private static final boolean DISABLE_FLUSH_CONSOLIDATION = Boolean.getBoolean("Paper.disableFlushConsolidate"); // PandaSpigot

    public void a(InetAddress inetaddress, int i) throws IOException {
        synchronized (this.g) {
            Class oclass;
            LazyInitVar lazyinitvar;

            if (Epoll.isAvailable() && this.f.ai()) {
                oclass = EpollServerSocketChannel.class;
                lazyinitvar = b;
            } else {
                oclass = NioServerSocketChannel.class;
                lazyinitvar = a;
            }

	        this.g.add(((new ServerBootstrap().channel(oclass)).childHandler(new ChannelInitializer() {
		        protected void initChannel(Channel channel) throws Exception {
			        try {
				        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                        channel.config().setOption(ChannelOption.IP_TOS, 0x18); // [Nacho-0027] :: Optimize networking
			        } catch (ChannelException channelexception) {
                        // Ignore
                    }

                    if (!DISABLE_FLUSH_CONSOLIDATION)
                        channel.pipeline().addFirst(new FlushConsolidationHandler()); // PandaSpigot

			        channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new LegacyPingHandler(net.minecraft.server.ServerConnection.this)).addLast("splitter", new PacketSplitter()).addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND)).addLast("prepender", new PacketPrepender()).addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));

			        NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);

                    pending.add(networkmanager); // Paper
			        channel.pipeline().addLast("packet_handler", networkmanager);
                    networkmanager.isPending = false; // PandaSpigot
			        networkmanager.a((new HandshakeListener(net.minecraft.server.ServerConnection.this.f, networkmanager)));
		        }
	        }).group((EventLoopGroup) lazyinitvar.c()).localAddress(inetaddress, i)).bind().syncUninterruptibly());
        }
    }

    public void b() {
        this.d = false;
        Iterator iterator = this.g.iterator();

        while (iterator.hasNext()) {
            ChannelFuture channelfuture = (ChannelFuture) iterator.next();

            try {
                channelfuture.channel().close().sync();
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
        return this.f;
    }

}
