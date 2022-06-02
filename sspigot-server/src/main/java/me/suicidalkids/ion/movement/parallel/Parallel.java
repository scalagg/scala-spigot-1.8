package me.suicidalkids.ion.movement.parallel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Parallel {

    @Getter
    protected Queue<Runnable> packets =
        new ConcurrentLinkedQueue<>();

    private boolean running;

    public Parallel() {
        final Thread thread = new Thread(this::loop);
        thread.start();

        this.running = true;
    }

    public void loop() {
        long lastTick = System.nanoTime();
        long catchupTime = 0L;

        while (running) {
            long curTime = System.nanoTime();
            int tickTime = 1000000000 / 60;
            long wait = (long) tickTime - (curTime - lastTick) - catchupTime;
            if (wait > 0L) {
                try {
                    Thread.sleep(wait / 1000000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catchupTime = 0L;
            } else {
                catchupTime = Math.min(1000000000L, Math.abs(wait));
                execute();
                lastTick = curTime;
            }
        }
    }

    public void execute() {
        while (this.packets.size() > 0) {
            this.packets.poll().run();
        }
    }

    public void addPacket(
        Packet<?> packet, NetworkManager manager,
        GenericFutureListener<? extends Future<? super Void>>[] genericFutureListener
    ) {
        this.packets.add(() -> ParallelChannelWriter.writeThenFlush(
            manager.channel, packet, genericFutureListener)
        );
    }
}
