package org.finetree.finechat.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Optional GroupManager hook.
 *
 * Uses reflection so the plugin can still load/run when GroupManager isn't installed.
 */
public class GroupManagerHook {

    private final Plugin plugin;

    public GroupManagerHook(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @return true if GroupManager plugin is installed and enabled.
     */
    public boolean isPresent() {
        try {
            Plugin gm = Bukkit.getPluginManager().getPlugin("GroupManager");
            return gm != null && gm.isEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public String getPrefix(Player player) {
        return getUserMeta(player, "getUserPrefix");
    }

    public String getSuffix(Player player) {
        return getUserMeta(player, "getUserSuffix");
    }

    private String getUserMeta(Player player, String handlerMethodName) {
        if (player == null) return null;

        try {
            Plugin gm = Bukkit.getPluginManager().getPlugin("GroupManager");
            if (gm == null || !gm.isEnabled()) return null;

            // GroupManager#getWorldsHolder()
            Object worldsHolder = gm.getClass().getMethod("getWorldsHolder").invoke(gm);
            if (worldsHolder == null) return null;

            // WorldsHolder#getWorldPermissions(Player)
            Object handler = worldsHolder.getClass()
                    .getMethod("getWorldPermissions", Player.class)
                    .invoke(worldsHolder, player);

            if (handler == null) return null;

            // AnjoPermissionsHandler#getUserPrefix(String) or #getUserSuffix(String)
            Object meta = handler.getClass()
                    .getMethod(handlerMethodName, String.class)
                    .invoke(handler, player.getName());

            if (meta == null) return null;

            return String.valueOf(meta);
        } catch (Throwable t) {
            // silent fail; let other hooks handle it
            return null;
        }
    }
}
