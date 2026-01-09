package org.finetree.finechat.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired before FineChat formats a chat message.
 * <p>
 * Other plugins can listen to this event to:
 * <ul>
 *   <li>Modify the message content</li>
 *   <li>Override the prefix or suffix</li>
 *   <li>Change the format template</li>
 *   <li>Cancel the message entirely</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * {@literal @}EventHandler
 * public void onFineChatFormat(FineChatFormatEvent event) {
 *     // Add a custom tag to VIP players
 *     if (event.getPlayer().hasPermission("vip")) {
 *         event.setPrefix("&6[VIP] &r" + event.getPrefix());
 *     }
 *     
 *     // Censor bad words
 *     event.setMessage(censorBadWords(event.getMessage()));
 * }
 * </pre>
 * <p>
 * Note: This event is called asynchronously. Be careful when accessing
 * Bukkit API that requires sync access.
 */
public class FineChatFormatEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private String message;
    private String prefix;
    private String suffix;
    private String format;
    private boolean cancelled;

    /**
     * Create a new FineChatFormatEvent.
     *
     * @param async   whether the event is async
     * @param player  the player sending the message
     * @param message the raw message content
     * @param prefix  the resolved prefix
     * @param suffix  the resolved suffix
     * @param format  the format template
     */
    public FineChatFormatEvent(boolean async, Player player, String message, String prefix, String suffix, String format) {
        super(async);
        this.player = player;
        this.message = message;
        this.prefix = prefix;
        this.suffix = suffix;
        this.format = format;
        this.cancelled = false;
    }

    /**
     * Get the player sending the message.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the message content.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message content.
     *
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the player's prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the player's prefix.
     *
     * @param prefix the new prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the player's suffix.
     *
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Set the player's suffix.
     *
     * @param suffix the new suffix
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Get the format template.
     * <p>
     * The format uses placeholders like {prefix}, {player}, {message}, etc.
     *
     * @return the format template
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the format template.
     * <p>
     * Available placeholders:
     * <ul>
     *   <li>{prefix} - Player's prefix</li>
     *   <li>{suffix} - Player's suffix</li>
     *   <li>{player} - Player's name</li>
     *   <li>{displayname} - Player's display name</li>
     *   <li>{message} - The message content</li>
     *   <li>{world} - Player's world name</li>
     * </ul>
     *
     * @param format the new format template
     */
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
