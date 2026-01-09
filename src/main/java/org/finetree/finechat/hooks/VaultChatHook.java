package org.finetree.finechat.hooks;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class VaultChatHook {

    private Chat chat;

    public boolean setup(Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        var rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        plugin.getLogger().info("[FineChat] Hooked Vault Chat: " + chat.getName());
        return true;
    }

    public boolean isHooked() {
        return chat != null;
    }

    public String getPrefix(Player player) {
        if (chat == null || player == null) return "";
        String prefix = chat.getPlayerPrefix(player);
        return prefix == null ? "" : prefix;
    }

    public String getSuffix(Player player) {
        if (chat == null || player == null) return "";
        String suffix = chat.getPlayerSuffix(player);
        return suffix == null ? "" : suffix;
    }
}
