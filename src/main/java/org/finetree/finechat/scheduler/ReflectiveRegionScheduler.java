package org.finetree.finechat.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Uses Paper/Folia threaded-region schedulers via reflection.
 *
 * Why reflection?
 * - We build against Spigot API so the jar runs on Spigot.
 * - Paper/Folia expose extra scheduler methods not present in Spigot.
 *
 * This class is intentionally tiny and defensive: if any reflection fails,
 * you should fall back to BukkitPlatformScheduler.
 */
public class ReflectiveRegionScheduler implements PlatformScheduler {

    private final Plugin plugin;

    private final Object asyncScheduler;          // Server.getAsyncScheduler()
    private final Object globalRegionScheduler;   // Server.getGlobalRegionScheduler()

    private final Method asyncRunNow;             // AsyncScheduler.runNow(Plugin, Consumer)
    private final Method globalExecute;           // GlobalRegionScheduler.execute(Plugin, Runnable)

    private final Method playerGetScheduler;      // Entity.getScheduler() (Player implements Entity)

    public ReflectiveRegionScheduler(Plugin plugin) throws ReflectiveOperationException {
        this.plugin = Objects.requireNonNull(plugin, "plugin");

        Server server = Bukkit.getServer();

        Method getAsyncScheduler = server.getClass().getMethod("getAsyncScheduler");
        Method getGlobalRegionScheduler = server.getClass().getMethod("getGlobalRegionScheduler");

        this.asyncScheduler = getAsyncScheduler.invoke(server);
        this.globalRegionScheduler = getGlobalRegionScheduler.invoke(server);

        // AsyncScheduler#runNow(Plugin, Consumer<ScheduledTask>)
        this.asyncRunNow = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);

        // GlobalRegionScheduler#execute(Plugin, Runnable)
        this.globalExecute = globalRegionScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);

        // Player#getScheduler() -> EntityScheduler
        this.playerGetScheduler = Player.class.getMethod("getScheduler");
    }

    public static boolean isSupported() {
        try {
            Server server = Bukkit.getServer();
            server.getClass().getMethod("getAsyncScheduler");
            server.getClass().getMethod("getGlobalRegionScheduler");
            Player.class.getMethod("getScheduler");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void runAsync(Runnable task) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            asyncRunNow.invoke(asyncScheduler, plugin, consumer);
        } catch (Throwable t) {
            // Fallback to Bukkit async if something goes wrong
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    @Override
    public void runSyncGlobal(Runnable task) {
        try {
            globalExecute.invoke(globalRegionScheduler, plugin, task);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void runSyncPlayer(Player player, Runnable task) {
        if (player == null) {
            runSyncGlobal(task);
            return;
        }
        try {
            Object entityScheduler = playerGetScheduler.invoke(player);

            // EntityScheduler#execute(Plugin, Runnable, Runnable, long)
            Method execute = entityScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);

            // delay 1 tick (Folia treats <1 as 1)
            execute.invoke(entityScheduler, plugin, task, null, 1L);
        } catch (Throwable t) {
            // Last resort fallback
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
}
