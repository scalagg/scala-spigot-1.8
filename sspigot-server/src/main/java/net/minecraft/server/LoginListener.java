package net.minecraft.server;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
// CraftBukkit end

public class LoginListener implements PacketLoginInListener, IUpdatePlayerListBox {

    private static final AtomicInteger b = new AtomicInteger(0);
    private static final Logger c = LogManager.getLogger();
    private static final Random random = new Random();
    public final NetworkManager networkManager;
    private final byte[] e = new byte[4];
    private final MinecraftServer server;
    public String hostname = ""; // CraftBukkit - add field
    private net.minecraft.server.LoginListener.EnumProtocolState g;
    private int h;
    private GameProfile i;
    private final String j;
    private SecretKey loginKey;
    private EntityPlayer l;

    public LoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.g = net.minecraft.server.LoginListener.EnumProtocolState.HELLO;
        this.j = "";
        this.server = minecraftserver;
        this.networkManager = networkmanager;
        random.nextBytes(this.e);
    }

    public void c() {
        if (this.g == net.minecraft.server.LoginListener.EnumProtocolState.READY_TO_ACCEPT) {
            this.b();
        } else if (this.g == net.minecraft.server.LoginListener.EnumProtocolState.e) {
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());

            if (entityplayer == null) {
                this.g = net.minecraft.server.LoginListener.EnumProtocolState.READY_TO_ACCEPT;
                this.server.getPlayerList().a(this.networkManager, this.l);
                this.l = null;
            }
        }

        if (this.h++ == 600) {
            this.disconnect("Took too long to log in");
        }

    }

    public void disconnect(String s) {
        try {
            c.info("Disconnecting " + this.d() + ": " + s);
            ChatComponentText chatcomponenttext = new ChatComponentText(s);

            this.networkManager.handle(new PacketLoginOutDisconnect(chatcomponenttext));
            this.networkManager.close(chatcomponenttext);
        } catch (Exception exception) {
            c.error("Error whilst disconnecting player", exception);
        }

    }

    // Spigot start
    public void initUUID() {
        UUID uuid;
        if (networkManager.spoofedUUID != null) {
            uuid = networkManager.spoofedUUID;
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.i.getName()).getBytes(Charsets.UTF_8));
        }

        this.i = new GameProfile(uuid, this.i.getName());

        if (networkManager.spoofedProfile != null) {
            for (com.mojang.authlib.properties.Property property : networkManager.spoofedProfile) {
                this.i.getProperties().put(property.getName(), property);
            }
        }
    }
    // Spigot end

    public void b() {
        // Spigot start - Moved to initUUID
        /*
        if (!this.i.isComplete()) {
            this.i = this.a(this.i);
        }
        */
        // Spigot end

        // CraftBukkit start - fire PlayerLoginEvent
        EntityPlayer s = this.server.getPlayerList().attemptLogin(this, this.i, hostname);

        if (s == null) {
            // this.disconnect(s);
            // CraftBukkit end
        } else {
            this.g = net.minecraft.server.LoginListener.EnumProtocolState.ACCEPTED;
            if (this.server.aK() >= 0 && !this.networkManager.c()) {
                this.networkManager.a(new PacketLoginOutSetCompression(this.server.aK()), new ChannelFutureListener() {
                    public void a(ChannelFuture channelfuture) throws Exception {
                        net.minecraft.server.LoginListener.this.networkManager.a(net.minecraft.server.LoginListener.this.server.aK());
                    }

                    public void operationComplete(ChannelFuture future) throws Exception { // CraftBukkit - fix decompile error
                        this.a(future);
                    }
                });
            }

            this.networkManager.handle(new PacketLoginOutSuccess(this.i));
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());

            if (entityplayer != null) {
                this.g = net.minecraft.server.LoginListener.EnumProtocolState.e;
                this.l = this.server.getPlayerList().processLogin(this.i, s); // CraftBukkit - add player reference
            } else {
                this.server.getPlayerList().a(this.networkManager, this.server.getPlayerList().processLogin(this.i, s)); // CraftBukkit - add player reference
            }
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        c.info(this.d() + " lost connection: " + ichatbasecomponent.c());
    }

    public String d() {
        String socketAddress = networkManager == null ? null : (networkManager.getSocketAddress() == null ? null : networkManager.getSocketAddress().toString());
        return this.i != null ? this.i + " (" + socketAddress + ")" : socketAddress;
    }

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.g == net.minecraft.server.LoginListener.EnumProtocolState.HELLO, "Unexpected hello packet");
        this.i = packetlogininstart.a();
        if (this.server.getOnlineMode() && !this.networkManager.c()) {
            this.g = net.minecraft.server.LoginListener.EnumProtocolState.KEY;
            this.networkManager.handle(new PacketLoginOutEncryptionBegin(this.j, this.server.Q().getPublic(), this.e));
        } else {
            // Spigot start
            initUUID();

            // ScalaSpigot start - Use the ForkJoinPool commonPool instead of new thread
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    new LoginHandler().fireEvents();
                } catch (Exception ex) {
                    disconnect("Failed to verify username!");
                    server.server.getLogger().log(Level.WARNING, "Exception verifying " + i.getName(), ex);
                }
            });
            // ScalaSpigot end
            // Spigot end
        }

    }

    public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.g == net.minecraft.server.LoginListener.EnumProtocolState.KEY, "Unexpected key packet");
        PrivateKey privatekey = this.server.Q().getPrivate();

        if (!Arrays.equals(this.e, packetlogininencryptionbegin.b(privatekey))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.loginKey = packetlogininencryptionbegin.a(privatekey);
            this.g = net.minecraft.server.LoginListener.EnumProtocolState.AUTHENTICATING;
            this.networkManager.a(this.loginKey);

            ForkJoinPool.commonPool().execute(() -> {
                GameProfile gameprofile = net.minecraft.server.LoginListener.this.i;

                try {
                    String s = (new BigInteger(MinecraftEncryption.a(net.minecraft.server.LoginListener.this.j, net.minecraft.server.LoginListener.this.server.Q().getPublic(), net.minecraft.server.LoginListener.this.loginKey))).toString(16);

                    net.minecraft.server.LoginListener.this.i = net.minecraft.server.LoginListener.this.server.aD().hasJoinedServer(new GameProfile(null, gameprofile.getName()), s);
                    if (net.minecraft.server.LoginListener.this.server.T()) {
                        c.warn("Failed to verify username but will let them in anyway!");
                        net.minecraft.server.LoginListener.this.i = net.minecraft.server.LoginListener.this.a(gameprofile);
                        net.minecraft.server.LoginListener.this.g = net.minecraft.server.LoginListener.EnumProtocolState.READY_TO_ACCEPT;
                    } else {
                        net.minecraft.server.LoginListener.this.disconnect("Failed to verify username!");
                        c.error("Username '" + gameprofile.getName() + "' tried to join with an invalid session"); // CraftBukkit - fix null pointer
                    }
                } catch (AuthenticationUnavailableException authenticationunavailableexception) {
                    if (net.minecraft.server.LoginListener.this.server.T()) {
                        c.warn("Authentication servers are down but will let them in anyway!");
                        net.minecraft.server.LoginListener.this.i = net.minecraft.server.LoginListener.this.a(gameprofile);
                        net.minecraft.server.LoginListener.this.g = net.minecraft.server.LoginListener.EnumProtocolState.READY_TO_ACCEPT;
                    } else {
                        net.minecraft.server.LoginListener.this.disconnect("Authentication servers are down. Please try again later, sorry!");
                        c.error("Couldn't verify username because servers are unavailable");
                    }
                    // CraftBukkit start - catch all exceptions
                } catch (Exception exception) {
                    disconnect("Failed to verify username!");
                    server.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), exception);
                    // CraftBukkit end
                }
            });
        }
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + gameprofile.getName()).getBytes(Charsets.UTF_8));

        return new GameProfile(uuid, gameprofile.getName());
    }
    // Spigot end

    enum EnumProtocolState {

        HELLO, KEY, AUTHENTICATING, READY_TO_ACCEPT, e, ACCEPTED;

        EnumProtocolState() {
        }
    }

    // Spigot start
    public class LoginHandler {

        public void fireEvents() throws Exception {
            String playerName = i.getName();
            java.net.InetAddress address = ((java.net.InetSocketAddress) networkManager.getSocketAddress()).getAddress();
            UUID uniqueId = i.getId();
            final org.bukkit.craftbukkit.CraftServer server = net.minecraft.server.LoginListener.this.server.server;

            AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
            server.getPluginManager().callEvent(asyncEvent);

            if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
                final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);

                if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                    event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
                }

                Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                    @Override
                    protected PlayerPreLoginEvent.Result evaluate() {
                        server.getPluginManager().callEvent(event);
                        return event.getResult();
                    }
                };

                net.minecraft.server.LoginListener.this.server.processQueue.add(waitable);
                if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                    disconnect(event.getKickMessage());
                    return;
                }
            } else {
                if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                    disconnect(asyncEvent.getKickMessage());
                    return;
                }
            }
            // CraftBukkit end
            c.info("Attempting login handle for " + net.minecraft.server.LoginListener.this.i.getName() + "...");
            net.minecraft.server.LoginListener.this.g = net.minecraft.server.LoginListener.EnumProtocolState.READY_TO_ACCEPT;
        }
    }
}
