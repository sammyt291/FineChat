package org.finetree.finechat.scheduler;

import org.bukkit.plugin.Plugin;

public final class PlatformSchedulers {

    private PlatformSchedulers() {}

    public static PlatformScheduler create(Plugin plugin) {
        // Prefer Paper/Folia region schedulers if present, else Bukkit.
        if (ReflectiveRegionScheduler.isSupported()) {
            try {
                return new ReflectiveRegionScheduler(plugin);
            } catch (Throwable ignored) {
                // fall through
            }
        }
        return new BukkitPlatformScheduler(plugin);
    }
}
