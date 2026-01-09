package org.finetree.finechat.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    private static final long MS_PER_TICK = 50L; // 20 ticks = 1000ms

    private final Plugin plugin;
    private final BukkitPlatformScheduler fallback;

    private final Object asyncScheduler;          // Server.getAsyncScheduler()
    private final Object globalRegionScheduler;   // Server.getGlobalRegionScheduler()

    private final Method asyncRunNow;             // AsyncScheduler.runNow(Plugin, Consumer)
    private final Method asyncRunDelayed;         // AsyncScheduler.runDelayed(Plugin, Consumer, long, TimeUnit)
    private final Method asyncRunAtFixedRate;     // AsyncScheduler.runAtFixedRate(Plugin, Consumer, long, long, TimeUnit)

    private final Method globalExecute;           // GlobalRegionScheduler.execute(Plugin, Runnable)
    private final Method globalRunDelayed;        // GlobalRegionScheduler.runDelayed(Plugin, Consumer, long)
    private final Method globalRunAtFixedRate;    // GlobalRegionScheduler.runAtFixedRate(Plugin, Consumer, long, long)

    private final Method playerGetScheduler;      // Entity.getScheduler() (Player implements Entity)

    public ReflectiveRegionScheduler(Plugin plugin) throws ReflectiveOperationException {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.fallback = new BukkitPlatformScheduler(plugin);

        Server server = Bukkit.getServer();

        Method getAsyncScheduler = server.getClass().getMethod("getAsyncScheduler");
        Method getGlobalRegionScheduler = server.getClass().getMethod("getGlobalRegionScheduler");

        this.asyncScheduler = getAsyncScheduler.invoke(server);
        this.globalRegionScheduler = getGlobalRegionScheduler.invoke(server);

        // AsyncScheduler methods
        this.asyncRunNow = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
        this.asyncRunDelayed = asyncScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
        this.asyncRunAtFixedRate = asyncScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);

        // GlobalRegionScheduler methods
        this.globalExecute = globalRegionScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
        this.globalRunDelayed = globalRegionScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
        this.globalRunAtFixedRate = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);

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

    // === Immediate tasks ===

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

            // EntityScheduler#execute(Plugin, Runnable, Runnable retired, long delay)
            Method execute = entityScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);

            // delay 1 tick (Folia treats <1 as 1)
            execute.invoke(entityScheduler, plugin, task, null, 1L);
        } catch (Throwable t) {
            // Last resort fallback
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    // === Delayed/Timer tasks ===

    @Override
    public ScheduledTask runAsyncLater(Runnable task, long delayTicks) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            long delayMs = delayTicks * MS_PER_TICK;
            Object foliaTask = asyncRunDelayed.invoke(asyncScheduler, plugin, consumer, delayMs, TimeUnit.MILLISECONDS);
            return new ScheduledTask(foliaTask);
        } catch (Throwable t) {
            return fallback.runAsyncLater(task, delayTicks);
        }
    }

    @Override
    public ScheduledTask runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            long delayMs = Math.max(1, delayTicks * MS_PER_TICK); // Folia requires initialDelayTicks > 0
            long periodMs = periodTicks * MS_PER_TICK;
            Object foliaTask = asyncRunAtFixedRate.invoke(asyncScheduler, plugin, consumer, delayMs, periodMs, TimeUnit.MILLISECONDS);
            return new ScheduledTask(foliaTask);
        } catch (Throwable t) {
            return fallback.runAsyncTimer(task, delayTicks, periodTicks);
        }
    }

    @Override
    public ScheduledTask runSyncGlobalLater(Runnable task, long delayTicks) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            Object foliaTask = globalRunDelayed.invoke(globalRegionScheduler, plugin, consumer, delayTicks);
            return new ScheduledTask(foliaTask);
        } catch (Throwable t) {
            return fallback.runSyncGlobalLater(task, delayTicks);
        }
    }

    @Override
    public ScheduledTask runSyncGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            Object foliaTask = globalRunAtFixedRate.invoke(globalRegionScheduler, plugin, consumer, delayTicks, periodTicks);
            return new ScheduledTask(foliaTask);
        } catch (Throwable t) {
            return fallback.runSyncGlobalTimer(task, delayTicks, periodTicks);
        }
    }

    @Override
    public ScheduledTask runSyncPlayerLater(Player player, Runnable task, long delayTicks) {
        if (player == null) {
            return runSyncGlobalLater(task, delayTicks);
        }
        try {
            Object entityScheduler = playerGetScheduler.invoke(player);

            // EntityScheduler#run(Plugin, Consumer<ScheduledTask>, Runnable retired, long delayTicks)
            Method run = entityScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class, long.class);

            Consumer<Object> consumer = ignored -> task.run();
            Object foliaTask = run.invoke(entityScheduler, plugin, consumer, null, delayTicks);

            // EntityScheduler.run returns null if entity is retired, wrap safely
            if (foliaTask != null) {
                return new ScheduledTask(foliaTask);
            }
            // Entity was retired (e.g., player disconnected), return a no-op task
            return new ScheduledTask((Object) null);
        } catch (Throwable t) {
            return fallback.runSyncPlayerLater(player, task, delayTicks);
        }
    }
}
