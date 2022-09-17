package gg.scala.server.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NamePriorityThreadFactory implements ThreadFactory {
    private int priority;
    private int idCounter = 0;
    private String name = "scalaspigot-thread";
    private boolean isDaemon = false;

    public NamePriorityThreadFactory(int priority) {
        this.priority = Math.min(Math.max(priority, Thread.MIN_PRIORITY), Thread.MAX_PRIORITY);
    }

    public NamePriorityThreadFactory(int priority, boolean daemon) {
        this(priority);
        this.isDaemon = daemon;
    }

    public NamePriorityThreadFactory(int priority, String name) {
        this(priority);
        this.name = name;
    }

    public NamePriorityThreadFactory(int priority, String name, boolean daemon) {
        this(priority, name);
        this.isDaemon = daemon;
    }

    public NamePriorityThreadFactory(String name) {
        this(Thread.NORM_PRIORITY);
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setPriority(this.priority);
        thread.setName(this.name + "-" + String.valueOf(idCounter));
        thread.setDaemon(this.isDaemon);
        idCounter++;
        return thread;
    }
}
