package org.finetree.finechat.scheduler;

import org.bukkit.entity.Player;

/**
 * Scheduler abstraction so the rest of the plugin doesn't care
 * whether we're on Spigot, Paper, or Folia.
 *
 * Immediate tasks:
 * - runAsync: background work
 * - runSyncGlobal: safe place for server/console operations
 * - runSyncPlayer: safe place for player operations (Folia region-safe)
 *
 * Delayed/Timer tasks (return ScheduledTask for cancellation):
 * - runAsyncLater: delayed background work
 * - runAsyncTimer: repeating background work
 * - runSyncGlobalLater: delayed global/main-thread work
 * - runSyncGlobalTimer: repeating global/main-thread work
 * - runSyncPlayerLater: delayed player-region work
 */
public interface PlatformScheduler {

    // === Immediate tasks (fire-and-forget) ===

    void runAsync(Runnable task);

    void runSyncGlobal(Runnable task);

    void runSyncPlayer(Player player, Runnable task);

    // === Delayed tasks (return ScheduledTask for cancellation) ===

    /**
     * Run a task asynchronously after a delay.
     *
     * @param task   the task to run
     * @param delayTicks delay in ticks (20 ticks = 1 second)
     * @return a ScheduledTask that can be cancelled
     */
    ScheduledTask runAsyncLater(Runnable task, long delayTicks);

    /**
     * Run a repeating task asynchronously.
     *
     * @param task        the task to run
     * @param delayTicks  initial delay in ticks
     * @param periodTicks period between executions in ticks
     * @return a ScheduledTask that can be cancelled
     */
    ScheduledTask runAsyncTimer(Runnable task, long delayTicks, long periodTicks);

    /**
     * Run a task on the global/main thread after a delay.
     *
     * @param task       the task to run
     * @param delayTicks delay in ticks
     * @return a ScheduledTask that can be cancelled
     */
    ScheduledTask runSyncGlobalLater(Runnable task, long delayTicks);

    /**
     * Run a repeating task on the global/main thread.
     *
     * @param task        the task to run
     * @param delayTicks  initial delay in ticks
     * @param periodTicks period between executions in ticks
     * @return a ScheduledTask that can be cancelled
     */
    ScheduledTask runSyncGlobalTimer(Runnable task, long delayTicks, long periodTicks);

    /**
     * Run a task on a player's region thread after a delay.
     * Falls back to global scheduler if player is null.
     *
     * @param player     the player (for Folia region scheduling)
     * @param task       the task to run
     * @param delayTicks delay in ticks
     * @return a ScheduledTask that can be cancelled
     */
    ScheduledTask runSyncPlayerLater(Player player, Runnable task, long delayTicks);
}
