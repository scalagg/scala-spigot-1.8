package me.suicidalkids.ion.movement.parallel;

import com.google.common.collect.Queues;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.Packet;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author GrowlyX
 * @since 6/2/2022
 */
@RequiredArgsConstructor
public class ParallelChannelWriter {
    private static final Queue<PacketQueue> queue = Queues.newConcurrentLinkedQueue();
    private static final Tasks tasks = new Tasks();

    private final Channel channel;

    public static void writeThenFlush(
        Channel channel, Packet<?> value,
        GenericFutureListener<? extends Future<? super Void>>[] listener
    ) {
        ParallelChannelWriter writer = new ParallelChannelWriter(channel);
        queue.add(new PacketQueue(value, listener));

        if (tasks.addTask()) {
            channel.pipeline().lastContext().executor().execute(writer::writeQueueAndFlush);
        }
    }

    public void writeQueueAndFlush() {
        while (tasks.fetchTask()) {
            while (queue.size() > 0) {
                PacketQueue messages = queue.poll();
                if (messages == null) continue;
                ChannelFuture future = this.channel.write(messages.getPacket());
                if (messages.getListener() != null) {
                    future.addListeners(messages.getListener());
                }
                future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }

        this.channel.flush();
    }

    @Getter
    @RequiredArgsConstructor
    private static class PacketQueue {
        private final Packet<?> packet;
        private final GenericFutureListener<? extends Future<? super Void>>[] listener;
    }

    private static class Tasks {
        private final AtomicInteger state = new AtomicInteger();

        public boolean fetchTask() {
            int old = this.state.getAndDecrement();
            if (old == State.RUNNING_GOT_TASKS.ordinal()) {
                return true;
            } else if (old == State.RUNNING_NO_TASKS.ordinal()) {
                return false;
            } else {
                throw new AssertionError();
            }
        }

        public boolean addTask() {
            if (this.state.get() == State.RUNNING_GOT_TASKS.ordinal()) {
                return false;
            } else {
                int old = this.state.getAndSet(State.RUNNING_GOT_TASKS.ordinal());
                return old == State.WAITING.ordinal();
            }
        }

        private enum State {
            WAITING,
            RUNNING_NO_TASKS,
            RUNNING_GOT_TASKS;
        }
    }
}
