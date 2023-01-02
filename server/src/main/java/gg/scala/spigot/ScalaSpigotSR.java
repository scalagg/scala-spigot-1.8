package gg.scala.spigot;

import gg.scala.spigot.async.thread.CombatThread;

public class ScalaSpigotSR {

//    private static ScalaSpigotSR INSTANCE;

    private static final ScalaSpigotSR INSTANCE = new ScalaSpigotSR();

    private CombatThread knockbackThread;

    public ScalaSpigotSR() {
//        INSTANCE = this;
        this.init();
    }

    public void reload() {
        this.init();
    }

    private void init() {
        if (ScalaSpigot.INSTANCE.getConfig().getFixHitReg()) {
            knockbackThread = new CombatThread("Knockback Thread");
        }
    }

    public CombatThread getKnockbackThread() {
        return knockbackThread;
    }

    public static ScalaSpigotSR getInstance() {
        return INSTANCE;
    }
}
