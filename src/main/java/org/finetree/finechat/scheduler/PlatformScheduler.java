package org.finetree.finechat.scheduler;

import org.bukkit.entity.Player;

/**
 * Small scheduler abstraction so the rest of the plugin doesn't care
 * whether we're on Spigot, Paper, or Folia.
 *
 * - runAsync: background work
 * - runSyncGlobal: safe place for server/console operations
 * - runSyncPlayer: safe place for player operations (Folia region-safe)
 */
public interface PlatformScheduler {
    void runAsync(Runnable task);
    void runSyncGlobal(Runnable task);
    void runSyncPlayer(Player player, Runnable task);
}
