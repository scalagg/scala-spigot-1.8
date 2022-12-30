package gg.scala.spigot;

import gg.scala.spigot.async.thread.CombatThread;
import gg.scala.spigot.hitdetection.LagCompensator;

public class ScalaSpigotSR {

//    private static ScalaSpigotSR INSTANCE;

    private static final ScalaSpigotSR INSTANCE = new ScalaSpigotSR();

    private CombatThread knockbackThread;
    private LagCompensator lagCompensator;

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

        lagCompensator = new LagCompensator();
    }

    public CombatThread getKnockbackThread() {
        return knockbackThread;
    }

    public LagCompensator getLagCompensator() {
        return lagCompensator;
    }

    public static ScalaSpigotSR getInstance() {
        return INSTANCE;
    }
}
