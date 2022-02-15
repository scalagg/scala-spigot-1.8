package org.bukkit.craftbukkit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.server.MinecraftServer;
import net.minecrell.terminalconsole.TerminalConsoleAppender;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static boolean useJline = true;
    public static boolean useConsole = true;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser() {
            {
                acceptsAll(listOf("nojline"), "Disables jline and emulates the vanilla console");
                acceptsAll(listOf("noconsole"), "Disables the console");
                acceptsAll(listOf("demo"), "Demo mode");
            }
        };

        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
        }

        if ((options == null) || (options.has("?"))) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (options.has("v")) {
            System.out.println(CraftServer.class.getPackage().getImplementationVersion());
        } else {
            try {
                if (options.has("nojline")) {
                    System.setProperty(TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY, "false");
                    useJline = false;
                }

                if (options.has("noconsole")) {
                    useConsole = false;
                    useJline = false; // Paper
                    System.setProperty(TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY, "false"); // Paper
                }

                int maxPermGen = 0; // In kb

                for (String s : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (s.startsWith("-XX:MaxPermSize")) {
                        maxPermGen = Integer.parseInt(s.replaceAll("[^\\d]", ""));
                        maxPermGen <<= 10 * ("kmg".indexOf(Character.toLowerCase(s.charAt(s.length() - 1))));
                    }
                }

                if (Float.parseFloat(System.getProperty("java.class.version")) < 52 && maxPermGen < (128 << 10)) { // 128mb
                    System.out.println("Warning, your max perm gen size is not set or less than 128mb. It is recommended you restart Java with the following argument: -XX:MaxPermSize=128M");
                    System.out.println("Please see http://www.spigotmc.org/wiki/changing-permgen-size/ for more details and more in-depth instructions.");
                }

                System.out.println("Libraries are loading, please wait...");

                MinecraftServer.main(options);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static List<String> listOf(String... params) {
        return Arrays.asList(params);
    }
}
