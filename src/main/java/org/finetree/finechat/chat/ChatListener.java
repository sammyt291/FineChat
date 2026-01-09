package org.finetree.finechat.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.finetree.finechat.api.event.FineChatFormatEvent;
import org.finetree.finechat.config.ChatConfig;
import org.finetree.finechat.scheduler.PlatformScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * We avoid AsyncPlayerChatEvent#setFormat entirely to prevent format-string issues
 * (e.g. String.format handling) and to keep full control on Paper/Folia.
 */
public class ChatListener implements Listener {

    private final PlatformScheduler scheduler;
    private final ChatFormatter formatter;

    public ChatListener(PlatformScheduler scheduler, ChatFormatter formatter) {
        this.scheduler = scheduler;
        this.formatter = formatter;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!ChatConfig.enabled) return;

        Player sender = event.getPlayer();
        String message = event.getMessage();

        // Snapshot recipients safely (Async event!)
        List<UUID> recipients = new ArrayList<>();
        for (Player p : event.getRecipients()) {
            recipients.add(p.getUniqueId());
        }

        if (ChatConfig.overrideVanillaChat) {
            event.setCancelled(true);
        }

        // Gather anything Bukkit-ish on the player's region thread (or main thread on Spigot)
        scheduler.runSyncPlayer(sender, () -> {
            // Now do formatting logic off-thread
            scheduler.runAsync(() -> {
                // Resolve prefix/suffix for the event
                String prefix = formatter.resolvePrefix(sender);
                String suffix = formatter.resolveSuffix(sender);
                String format = ChatConfig.format;

                // Fire the API event (async)
                FineChatFormatEvent formatEvent = new FineChatFormatEvent(
                        true, sender, message, prefix, suffix, format
                );
                Bukkit.getPluginManager().callEvent(formatEvent);

                // Check if cancelled
                if (formatEvent.isCancelled()) {
                    return;
                }

                // Use potentially modified values from the event
                String formatted = formatter.format(
                        sender,
                        formatEvent.getMessage(),
                        formatEvent.getPrefix(),
                        formatEvent.getSuffix(),
                        formatEvent.getFormat()
                );

                // Send back on the right threads
                for (UUID uuid : recipients) {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target == null) continue;

                    scheduler.runSyncPlayer(target, () -> target.sendMessage(formatted));
                }

                // Also log to console (global thread is fine)
                scheduler.runSyncGlobal(() -> Bukkit.getConsoleSender().sendMessage(formatted));
            });
        });
    }
}
