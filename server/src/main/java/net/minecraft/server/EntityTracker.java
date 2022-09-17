package net.minecraft.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import gg.scala.server.IndexedLinkedHashSet;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityTracker {

    private static final Logger a = MinecraftServer.LOGGER;
    private final WorldServer world;

    public IndexedLinkedHashSet<EntityTrackerEntry> c = new IndexedLinkedHashSet<>(); // IonSpigot - HashSet -> ObjectMapList
    public Set<EntityTrackerEntry> getTrackedEntities() { return c; }

    public IntHashMap<EntityTrackerEntry> trackedEntities = new IntHashMap<>();
    public IntHashMap<EntityTrackerEntry> getTrackedEntityHashTable() { return trackedEntities; }

    private int e;

    private int noTrackDistance = 0;

    public int getNoTrackDistance() {
        return this.noTrackDistance;
    }

    public void setNoTrackDistance(int noTrackDistance) {
        this.noTrackDistance = noTrackDistance;
    }

    public EntityTracker(WorldServer worldserver) {
        this.world = worldserver;
        this.e = 128; // MineHQ
    }

    public void track(Entity entity) {
        if (entity instanceof EntityPlayer) {
            this.addEntity(entity, 512, 2);
            /*
            EntityPlayer entityplayer = (EntityPlayer) entity;
            Iterator iterator = this.c.iterator();

            while (iterator.hasNext()) {
                EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) iterator.next();

                if (entitytrackerentry.tracker != entityplayer) {
                    entitytrackerentry.updatePlayer(entityplayer);
                }
            }
            */
        } else if (entity instanceof EntityFishingHook) {
            this.addEntity(entity, 64, 5, true);
        } else if (entity instanceof EntityArrow) {
            this.addEntity(entity, 64, 20, false);
        } else if (entity instanceof EntitySmallFireball) {
            this.addEntity(entity, 64, 10, false);
        } else if (entity instanceof EntityFireball) {
            this.addEntity(entity, 64, 10, false);
        } else if (entity instanceof EntitySnowball) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityEnderPearl) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityEnderSignal) {
            this.addEntity(entity, 64, 4, true);
        } else if (entity instanceof EntityEgg) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityPotion) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityThrownExpBottle) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityFireworks) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityItem) {
            this.addEntity(entity, 64, 20, true);
        } else if (entity instanceof EntityMinecartAbstract) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityBoat) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntitySquid) {
            this.addEntity(entity, 64, 3, true);
        } else if (entity instanceof EntityWither) {
            this.addEntity(entity, 80, 3, false);
        } else if (entity instanceof EntityBat) {
            this.addEntity(entity, 80, 3, false);
        } else if (entity instanceof EntityEnderDragon) {
            this.addEntity(entity, 160, 3, true);
        } else if (entity instanceof IAnimal) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityTNTPrimed) {
            this.addEntity(entity, 160, 10, true);
        } else if (entity instanceof EntityFallingBlock) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityHanging) {
            this.addEntity(entity, 160, Integer.MAX_VALUE, false);
        } else if (entity instanceof EntityArmorStand) {
            this.addEntity(entity, 160, 3, true);
        } else if (entity instanceof EntityExperienceOrb) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityEnderCrystal) {
            this.addEntity(entity, 256, Integer.MAX_VALUE, false);
        }

    }

    public void addEntity(Entity entity, int i, int j) {
        this.addEntity(entity, i, j, false);
    }

    public void addEntity(Entity entity, int i, final int j, boolean flag) {
        org.spigotmc.AsyncCatcher.catchOp("entity track"); // Spigot
        i = org.spigotmc.TrackingRange.getEntityTrackingRange(entity, i); // Spigot
        if (i > this.e) {
            i = this.e;
        }

        try {
            if (this.trackedEntities.b(entity.getId())) {
                throw new IllegalStateException("Entity is already tracked!");
            }

            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(this, entity, i, j, flag);

            this.c.add(entitytrackerentry);
            this.trackedEntities.a(entity.getId(), entitytrackerentry);
            // entitytrackerentry.scanPlayers(this.world.players);
            entitytrackerentry.addNearPlayers();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void untrackEntity(Entity entity) {
        org.spigotmc.AsyncCatcher.catchOp( "entity untrack"); // Spigot
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            Iterator iterator = this.c.iterator();

            while (iterator.hasNext()) {
                EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) iterator.next();

                entitytrackerentry.a(entityplayer);
            }
        }

        EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry) this.trackedEntities.d(entity.getId());

        if (entitytrackerentry1 != null) {
            this.c.remove(entitytrackerentry1);
            entitytrackerentry1.a();
        }

    }

    private static final int TRACKER_THREADS = 4; // <-- 3 non-this threads, one this

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(
            TRACKER_THREADS - 1, new ThreadFactoryBuilder().setNameFormat("entity-tracker-%d").build()
    );

    public void updatePlayers() {
        final CountDownLatch latch = new CountDownLatch(TRACKER_THREADS);
        int offset = 0;

        for (int i = 1; i <= TRACKER_THREADS; i++) {
            final int localOffset = offset++;

            final Runnable runnable = () -> {
                for (
                    int i1 = localOffset;
                    i1 < c.size();
                    i1 += TRACKER_THREADS
                )
                {
                    c.get(i1).update();
                }

                latch.countDown();
            };

            if (i < TRACKER_THREADS)
            {
                THREAD_POOL.execute(runnable);
            } else
            {
                runnable.run();
            }
        }

        try {
            latch.await();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void a(Entity entity, Packet packet) {
        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) this.trackedEntities.get(entity.getId());

        if (entitytrackerentry != null) {
            entitytrackerentry.broadcast(packet);
        }

    }

    public void sendPacketToEntity(Entity entity, Packet packet) {
        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) this.trackedEntities.get(entity.getId());

        if (entitytrackerentry != null) {
            entitytrackerentry.broadcastIncludingSelf(packet);
        }

    }

    public void untrackPlayer(EntityPlayer entityplayer) {
        Iterator iterator = this.c.iterator();

        while (iterator.hasNext()) {
            EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) iterator.next();

            entitytrackerentry.clear(entityplayer, true);
        }

    }

//    public void a(EntityPlayer entityplayer, Chunk chunk) {
//        for (List<Entity> slice : chunk.entitySlices) { // Manny - Optimize entity tracker
//            for (Entity entity : slice) {
//                if (entity != entityplayer) {
//                    EntityTrackerEntry entry = trackedEntities.get(entity.getId());
//
//                    if (entry != null) {
//                        entry.updatePlayer(entityplayer);
//                    }
//                }
//            }
//        }
//    }
}
