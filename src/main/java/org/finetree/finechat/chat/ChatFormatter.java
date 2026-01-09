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

    /**
     * Resolve the prefix for a player from all configured sources.
     * Priority: GroupManager -> Vault -> LuckPerms
     *
     * @param player the player
     * @return the resolved prefix (never null, may be empty)
     */
    public String resolvePrefix(Player player) {
        String prefix = "";

        // 1) GroupManager priority (if present)
        if (groupManager != null && groupManager.isPresent()) {
            String gmPrefix = groupManager.getPrefix(player);
            if (!isBlank(gmPrefix)) prefix = gmPrefix;
        }

        // 2) Vault fills missing values (if configured)
        if (isBlank(prefix) && ChatConfig.preferVaultChat && vault != null && vault.isHooked()) {
            prefix = vault.getPrefix(player);
        }

        // 3) LuckPerms fallback fills anything still missing (if configured)
        if (isBlank(prefix) && ChatConfig.useLuckPermsGroupManager && luckPerms != null && luckPerms.isHooked()) {
            prefix = luckPerms.getPrefix(player);
        }

        return prefix == null ? "" : prefix;
    }

    /**
     * Resolve the suffix for a player from all configured sources.
     * Priority: GroupManager -> Vault -> LuckPerms
     *
     * @param player the player
     * @return the resolved suffix (never null, may be empty)
     */
    public String resolveSuffix(Player player) {
        String suffix = "";

        // 1) GroupManager priority (if present)
        if (groupManager != null && groupManager.isPresent()) {
            String gmSuffix = groupManager.getSuffix(player);
            if (!isBlank(gmSuffix)) suffix = gmSuffix;
        }

        // 2) Vault fills missing values (if configured)
        if (isBlank(suffix) && ChatConfig.preferVaultChat && vault != null && vault.isHooked()) {
            suffix = vault.getSuffix(player);
        }

        // 3) LuckPerms fallback fills anything still missing (if configured)
        if (isBlank(suffix) && ChatConfig.useLuckPermsGroupManager && luckPerms != null && luckPerms.isHooked()) {
            suffix = luckPerms.getSuffix(player);
        }

        return suffix == null ? "" : suffix;
    }

    public String format(Player player, String rawMessage) {
        return format(player, rawMessage, null, null, null);
    }

    /**
     * Format a chat message with optional overrides.
     *
     * @param player      the player
     * @param rawMessage  the raw message
     * @param prefixOverride override prefix (null to use resolved)
     * @param suffixOverride override suffix (null to use resolved)
     * @param formatOverride override format (null to use config)
     * @return the formatted message
     */
    public String format(Player player, String rawMessage, String prefixOverride, String suffixOverride, String formatOverride) {
        String prefix = prefixOverride != null ? prefixOverride : resolvePrefix(player);
        String suffix = suffixOverride != null ? suffixOverride : resolveSuffix(player);

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
        vars.put("{prefix}", prefix);
        vars.put("{suffix}", suffix);
        vars.put("{player}", player.getName());
        vars.put("{displayname}", display == null ? player.getName() : display);
        vars.put("{message}", processedMsg);
        vars.put("{world}", world);

        String out = formatOverride != null ? formatOverride : ChatConfig.format;
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
