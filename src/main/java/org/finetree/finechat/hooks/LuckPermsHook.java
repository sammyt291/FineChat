package org.finetree.finechat.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LuckPermsHook {

    private LuckPerms api;

    public boolean setup(Plugin plugin) {
        try {
            api = Bukkit.getServicesManager().load(LuckPerms.class);
            if (api != null) {
                plugin.getLogger().info("[FineChat] Hooked LuckPerms API");
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public boolean isHooked() {
        return api != null;
    }

    public String getPrefix(Player player) {
        if (api == null || player == null) return "";
        var user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        CachedMetaData meta = user.getCachedData().getMetaData();
        String prefix = meta.getPrefix();
        return prefix == null ? "" : prefix;
    }

    public String getSuffix(Player player) {
        if (api == null || player == null) return "";
        var user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        CachedMetaData meta = user.getCachedData().getMetaData();
        String suffix = meta.getSuffix();
        return suffix == null ? "" : suffix;
    }
}
