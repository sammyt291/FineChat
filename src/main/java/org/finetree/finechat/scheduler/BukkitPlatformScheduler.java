package org.finetree.finechat.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BukkitPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;

    public BukkitPlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

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
}
