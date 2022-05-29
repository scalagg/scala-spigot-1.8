/*
 * This file is licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018 Daniel Ennis <http://aikar.co>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.minecraft.server;

import com.google.common.util.concurrent.MoreExecutors;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PaperAsyncChunkLoader {

    private static final ExecutorService chunkExecutor = Executors.newCachedThreadPool(new NamedIncrementingThreadFactory("Paper Chunk Executor"));
    private static final ExecutorService directChunkExecutor = MoreExecutors.newDirectExecutorService();

    private final IAsyncTaskHandler asyncTaskHandler;
    private final ChunkProviderServer server;
    private final WorldServer world;
    private final IChunkLoader chunkLoader;
    private final ChunkTaskScheduler chunkTaskScheduler;

    private final Long2ObjectMap<PendingChunk> pendingChunks = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());


    PaperAsyncChunkLoader(WorldServer world, ChunkProviderServer server, IAsyncTaskHandler asynctaskhandler, IChunkLoader chunkloader, ChunkTaskScheduler chunkTaskScheduler) {
        this.server = server;
        this.world = world;
        this.asyncTaskHandler = asynctaskhandler;
        this.chunkLoader = chunkloader;
        this.chunkTaskScheduler = chunkTaskScheduler;
    }

    Chunk loadOrGenerateChunk(int x, int z, boolean gen, Consumer<Chunk> consumer) {
        long key = ChunkCoordIntPair.a(x, z);
        AtomicBoolean hadPending = new AtomicBoolean(false);

        final CompletableFuture<Chunk> future = new CompletableFuture<>();
        final PendingChunk pendingChunk = pendingChunks.compute(key, (prevKey, pending) -> {
            if (pending == null) {
                pending = new PendingChunk(x, z, key, gen);
            } else {
                hadPending.set(true);
            }

            if (!gen && pending.generating) {
                future.complete(null);
            } else {
                pending.addListener(future, gen);
            }
            return pending;
        });


        if (!hadPending.get()) {
            final boolean useDirect = !Bukkit.isPrimaryThread() || consumer == null;
            if (useDirect) {
                directChunkExecutor.submit(pendingChunk);
            } else {
                chunkExecutor.submit(pendingChunk);
            }
        }

        if (consumer == null) {
            try (co.aikar.timings.Timing timing = world.timings.syncChunkLoadTimer.startTiming()) {
                return future.join();
            }
        } else {
            consumeOnMain(future, consumer);
        }
        return null;
    }

    private Chunk loadChunk(int x, int z) {
        try {
            return this.chunkLoader.loadChunkAt(this.world, x, z, this::doNothingChunkConsumer);
        } catch (IOException e) {
            MinecraftServer.LOGGER.error("Couldn't load chunk", e);
            return null;
        }
    }
    private Chunk generateChunk(int x, int z) {
        try (co.aikar.timings.Timing timing = world.timings.chunkGeneration.startTiming()) {
            CompletableFuture<ProtoChunk> pending = this.chunkTaskScheduler.a(new ChunkCoordIntPair(x, z));

            return new Chunk(this.world, pending.join(), x, z);
        } catch (RuntimeException runtimeexception) {
            throw this.server.generateChunkError(x, z, runtimeexception);
        }
    }

    private void consumeOnMain(CompletableFuture<Chunk> future, Consumer<Chunk> consumer) {
        future.thenAccept((c) -> this.asyncTaskHandler.postToMainThread(() -> consumer.accept(c)));
    }

    private void doNothingChunkConsumer(Chunk chunk) {}


    static AtomicInteger idpool = new AtomicInteger(1);

    public boolean chunkGoingToExists(int x, int z) {
        synchronized (pendingChunks) {
            long key = ChunkCoordIntPair.a(x, z);
            PendingChunk pendingChunk = pendingChunks.get(key);
            return pendingChunk != null && pendingChunk.canGenerate;
        }
    }

    private class PendingChunk implements Runnable {
        private final int x;
        private final int z;
        private final long key;

        volatile boolean generating;
        volatile boolean canGenerate;

        final CompletableFuture<Chunk> loadOnly = new CompletableFuture<>();
        final CompletableFuture<Chunk> generate = new CompletableFuture<>();
        private String debug;



        PendingChunk(int x, int z, long key, boolean canGenerate) {
            this.x = x;
            this.z = z;
            this.key = key;
            this.canGenerate = canGenerate;
            debug = "(" + idpool.getAndIncrement() + ") " + x + "," + z;
        }
        boolean loadFinished(Chunk chunk) {
            Chunk other = checkAddChunk(chunk);
            boolean shouldGenerate;
            synchronized (pendingChunks) {
                shouldGenerate = chunk == null && other == null && canGenerate;
                if (!shouldGenerate) {
                    pendingChunks.remove(key);
                } else {
                    generating = true;
                }
            }
            if (other != null) {
                loadOnly.complete(other);
                generate.complete(other);
                return false;
            } else if (chunk != null) {
                chunk.setLastSaved(PaperAsyncChunkLoader.this.world.getTime());
                loadOnly.complete(chunk);
                generate.complete(chunk);
                addEntities(chunk);
            } else {
                // this is nullable for anyone who doesn't need generation
                loadOnly.complete(null);
                if (!shouldGenerate) {
                    generate.complete(null);
                }
            }

            return shouldGenerate;
        }

        private void addEntities(Chunk chunk) {
            PaperAsyncChunkLoader.this.asyncTaskHandler.postToMainThread(chunk::addEntities);
        }

        private Chunk checkAddChunk(Chunk chunk) {
            synchronized (server.chunks) {
                final Chunk other = server.chunks.get(key);
                if (other == null && chunk != null) {
                    server.chunks.put(key, chunk);
                }
                return other;
            }
        }

        void generateFinished(Chunk chunk) {
            Chunk other = checkAddChunk(chunk);
            synchronized (pendingChunks) {
                pendingChunks.remove(key);
            }
            if (other != null) {
                chunk = other;
            } else if (chunk != null) {
                addEntities(chunk);
            }
            generate.complete(chunk);
        }
        void addListener(CompletableFuture<Chunk> future, boolean gen) {
            if (gen) {
                canGenerate = true;
                generate.thenAccept(future::complete);
            } else {
                loadOnly.thenAccept(future::complete);
            }
        }

        @Override
        public void run() {
            Chunk chunk = loadChunk(x, z);
            if (!loadFinished(chunk)) {
                return;
            }

            System.out.println("GENERATE" + debug);
            try {
                chunk = generateChunk(x, z);
                System.out.println("GENERATE DONE " + debug);
                generateFinished(chunk);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("GENERATE ERROR " + debug);
                generateFinished(null);
            }

        }
    }

}
