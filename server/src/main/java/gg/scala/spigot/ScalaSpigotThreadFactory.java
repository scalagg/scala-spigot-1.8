package gg.scala.spigot;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ScalaSpigotThreadFactory implements ThreadFactory {

    private final int priority;
    private int idCounter = 0;
    private String name = "ScalaSpigotThread";

    public ScalaSpigotThreadFactory(int priority) {
        this.priority = Math.max(priority, Thread.MIN_PRIORITY);
    }

    public ScalaSpigotThreadFactory(int priority, String name) {
        this(priority);
        this.name = this.name + "-" + name;
    }

    public ScalaSpigotThreadFactory(String name) {
        this(Thread.NORM_PRIORITY);
        this.name = this.name + "-" + name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setPriority(this.priority);
        thread.setName(this.name + "-" + idCounter);
        idCounter++;
        return thread;
    }
}
