package org.finetree.finechat.scheduler;

import org.bukkit.scheduler.BukkitTask;

/**
 * Platform-agnostic wrapper for scheduled tasks.
 * Handles both Bukkit's BukkitTask and Folia's ScheduledTask via reflection.
 *
 * This allows code to schedule tasks and cancel them without knowing
 * whether the server is running Bukkit/Spigot/Paper or Folia.
 */
public class ScheduledTask {

    private final Object foliaTask;    // io.papermc.paper.threadedregions.scheduler.ScheduledTask
    private final BukkitTask bukkitTask;

    /**
     * Wrap a Folia ScheduledTask (can be null if entity was retired).
     */
    ScheduledTask(Object foliaTask) {
        this.foliaTask = foliaTask;
        this.bukkitTask = null;
    }

    /**
     * Wrap a Bukkit BukkitTask.
     */
    ScheduledTask(BukkitTask bukkitTask) {
        this.foliaTask = null;
        this.bukkitTask = bukkitTask;
    }

    /**
     * Cancel this task.
     * Safe to call even if the task is null, already cancelled, or already completed.
     */
    public void cancel() {
        if (foliaTask != null) {
            try {
                // io.papermc.paper.threadedregions.scheduler.ScheduledTask#cancel()
                foliaTask.getClass().getMethod("cancel").invoke(foliaTask);
            } catch (Throwable ignored) {
                // Task may already be cancelled or completed
            }
        } else if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        // If both are null, this is a no-op (e.g., retired entity task)
    }

    /**
     * Check if this task has been cancelled.
     *
     * @return true if cancelled, false otherwise (or if task is null/unknown state)
     */
    public boolean isCancelled() {
        if (foliaTask != null) {
            try {
                // ScheduledTask#isCancelled()
                return (boolean) foliaTask.getClass().getMethod("isCancelled").invoke(foliaTask);
            } catch (Throwable t) {
                return false;
            }
        } else if (bukkitTask != null) {
            return bukkitTask.isCancelled();
        }
        // Null task (e.g., retired entity) - treat as "not active" rather than cancelled
        return true;
    }

    /**
     * Check if this task reference is valid (non-null underlying task).
     *
     * @return true if there's an actual task to cancel/check
     */
    public boolean isValid() {
        return foliaTask != null || bukkitTask != null;
    }

    /**
     * Get the underlying Bukkit task, if this is a Bukkit-based task.
     *
     * @return the BukkitTask, or null for Folia tasks
     */
    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    /**
     * Get the underlying Folia task object, if this is a Folia-based task.
     *
     * @return the Folia ScheduledTask object, or null for Bukkit tasks
     */
    public Object getFoliaTask() {
        return foliaTask;
    }
}
