package org.finetree.finechat.chat;

import org.bukkit.entity.Player;
import org.finetree.finechat.config.ChatConfig;
import org.finetree.finechat.hooks.PlaceholderApiHook;
import org.finetree.finechat.hooks.GroupManagerHook;
import org.finetree.finechat.hooks.LuckPermsHook;
import org.finetree.finechat.hooks.VaultChatHook;
import org.finetree.finechat.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;

public class ChatFormatter {

    private final GroupManagerHook groupManager;
    private final VaultChatHook vault;
    private final LuckPermsHook luckPerms;
    private final PlaceholderApiHook papi;

    public ChatFormatter(GroupManagerHook groupManager, VaultChatHook vault, LuckPermsHook luckPerms, PlaceholderApiHook papi) {
        this.groupManager = groupManager;
        this.vault = vault;
        this.luckPerms = luckPerms;
        this.papi = papi;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public String format(Player player, String rawMessage) {
        String prefix = "";
        String suffix = "";

        // 1) GroupManager priority (if present)
        if (groupManager != null && groupManager.isPresent()) {
            String gmPrefix = groupManager.getPrefix(player);
            String gmSuffix = groupManager.getSuffix(player);

            if (!isBlank(gmPrefix)) prefix = gmPrefix;
            if (!isBlank(gmSuffix)) suffix = gmSuffix;
        }

        // 2) Vault fills missing values (if configured)
        if (ChatConfig.preferVaultChat && vault != null && vault.isHooked()) {
            if (isBlank(prefix)) prefix = vault.getPrefix(player);
            if (isBlank(suffix)) suffix = vault.getSuffix(player);
        }

        // 3) LuckPerms fallback fills anything still missing (if configured)
        if (ChatConfig.useLuckPermsGroupManager && luckPerms != null && luckPerms.isHooked()) {
            if (isBlank(prefix)) prefix = luckPerms.getPrefix(player);
            if (isBlank(suffix)) suffix = luckPerms.getSuffix(player);
        }

        String display = player.getDisplayName();
        String world = player.getWorld() != null ? player.getWorld().getName() : "";

        String msg = rawMessage == null ? "" : rawMessage;

        // Permission gating for colors
        boolean allowColor = player.hasPermission(ChatConfig.permChatColor);
        boolean allowSpecial = player.hasPermission(ChatConfig.permChatSpecialColor);

        String processedMsg = msg;

        if (!allowColor && ChatConfig.stripColorsIfNoPerm) {
            // Strip both '&' and '§' colors from message
            processedMsg = processedMsg.replaceAll("(?i)[&§][0-9a-fk-or]", "");
            processedMsg = processedMsg.replaceAll("(?i)§x(§[0-9a-f]){6}", "");
        } else {
            // If they can color, translate '&' later and keep '§' as-is
            if (!allowSpecial) {
                // Remove special tags if not permitted (leaves text readable)
                processedMsg = processedMsg
                        .replaceAll("(?i)</?rainbow\\s*>", "")
                        .replaceAll("(?i)</?hex\\s*,?[^>]*>", "")
                        .replaceAll("(?i)</?gradient\\s*,?[^>]*>", "");
            }
        }

        Map<String, String> vars = new HashMap<>();
        vars.put("{prefix}", prefix == null ? "" : prefix);
        vars.put("{suffix}", suffix == null ? "" : suffix);
        vars.put("{player}", player.getName());
        vars.put("{displayname}", display == null ? player.getName() : display);
        vars.put("{message}", processedMsg);
        vars.put("{world}", world);

        String out = ChatConfig.format;
        for (var e : vars.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }

        // PlaceholderAPI last (so placeholders can see our computed fields)
        out = (papi != null) ? papi.apply(player, out) : out;

        // Special tags + & -> § (config always allowed; message already gated above)
        out = ColorUtil.colorize(out, true);

        return out;
    }
}
