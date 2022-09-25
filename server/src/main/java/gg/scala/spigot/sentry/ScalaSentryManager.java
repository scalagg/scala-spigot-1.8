package gg.scala.spigot.sentry;

import gg.scala.spigot.ScalaSpigot;
import gg.scala.spigot.logger.Logger;
import io.sentry.Sentry;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;

public class ScalaSentryManager {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized || !ScalaSpigot.INSTANCE.getConfig().getSentrySettings().getInitialize()) {
            return;
        }

        try {
            Sentry.init(options -> {
                options.setDsn(ScalaSpigot.INSTANCE.getConfig().getSentrySettings().getDsn());
                options.setMaxBreadcrumbs(100);

                ScalaSentryAppender appender = new ScalaSentryAppender();
                appender.start();
                ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);

                Logger.send("Sentry has been configured!");
            });

            initialized = true;
        } catch (Exception exception) {
            MinecraftServer.LOGGER.error("Failed to configure sentry!", exception);
            initialized = false;
        }
    }
}
