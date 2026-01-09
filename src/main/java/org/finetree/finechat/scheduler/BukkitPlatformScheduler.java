package org.finetree.finechat.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;

    public BukkitPlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    // === Immediate tasks ===

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runSyncGlobal(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runSyncPlayer(Player player, Runnable task) {
        // On Spigot/Paper this is fine. On Folia, this may not be region-safe,
        // so the reflective scheduler is preferred if available.
        Bukkit.getScheduler().runTask(plugin, task);
    }

    // === Delayed/Timer tasks ===

    @Override
    public ScheduledTask runAsyncLater(Runnable task, long delayTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        return new ScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        return new ScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask runSyncGlobalLater(Runnable task, long delayTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        return new ScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask runSyncGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return new ScheduledTask(bukkitTask);
    }

    @Override
    public ScheduledTask runSyncPlayerLater(Player player, Runnable task, long delayTicks) {
        // On Spigot/Paper, player-specific scheduling is the same as global
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        return new ScheduledTask(bukkitTask);
    }
}
