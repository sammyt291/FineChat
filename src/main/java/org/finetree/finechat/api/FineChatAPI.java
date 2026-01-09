package org.finetree.finechat.api;

import org.bukkit.entity.Player;
import org.finetree.finechat.FineChat;
import org.finetree.finechat.chat.ChatFormatter;

/**
 * Public API for FineChat.
 * <p>
 * Other plugins can use this API to:
 * <ul>
 *   <li>Format messages using FineChat's formatter</li>
 *   <li>Get a player's resolved prefix/suffix</li>
 *   <li>Access the underlying formatter for advanced use</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * if (FineChatAPI.isAvailable()) {
 *     String formatted = FineChatAPI.formatMessage(player, "Hello world!");
 *     String prefix = FineChatAPI.getPrefix(player);
 * }
 * </pre>
 */
public final class FineChatAPI {

    private static FineChat plugin;
    private static ChatFormatter formatter;

    private FineChatAPI() {
        // Static API - no instantiation
    }

    /**
     * Initialize the API. Called internally by FineChat on enable.
     *
     * @param plugin    the FineChat plugin instance
     * @param formatter the chat formatter instance
     */
    public static void init(FineChat plugin, ChatFormatter formatter) {
        FineChatAPI.plugin = plugin;
        FineChatAPI.formatter = formatter;
    }

    /**
     * Shutdown the API. Called internally by FineChat on disable.
     */
    public static void shutdown() {
        plugin = null;
        formatter = null;
    }

    /**
     * Check if FineChat API is available and ready to use.
     *
     * @return true if the API is initialized and ready
     */
    public static boolean isAvailable() {
        return plugin != null && plugin.isEnabled() && formatter != null;
    }

    /**
     * Get the FineChat plugin instance.
     *
     * @return the plugin instance, or null if not available
     */
    public static FineChat getPlugin() {
        return plugin;
    }

    /**
     * Get the chat formatter instance.
     *
     * @return the formatter, or null if not available
     */
    public static ChatFormatter getFormatter() {
        return formatter;
    }

    /**
     * Format a message for a player using FineChat's chat format.
     * <p>
     * This applies the configured format, prefix, suffix, placeholders,
     * and color processing.
     *
     * @param player  the player to format for
     * @param message the raw message content
     * @return the fully formatted message, or the original message if API unavailable
     */
    public static String formatMessage(Player player, String message) {
        if (!isAvailable()) {
            return message;
        }
        return formatter.format(player, message);
    }

    /**
     * Get the resolved prefix for a player.
     * <p>
     * This checks GroupManager, Vault, and LuckPerms in priority order
     * based on the plugin configuration.
     *
     * @param player the player
     * @return the prefix (may be empty string, never null)
     */
    public static String getPrefix(Player player) {
        if (!isAvailable()) {
            return "";
        }
        return formatter.resolvePrefix(player);
    }

    /**
     * Get the resolved suffix for a player.
     * <p>
     * This checks GroupManager, Vault, and LuckPerms in priority order
     * based on the plugin configuration.
     *
     * @param player the player
     * @return the suffix (may be empty string, never null)
     */
    public static String getSuffix(Player player) {
        if (!isAvailable()) {
            return "";
        }
        return formatter.resolveSuffix(player);
    }
}
