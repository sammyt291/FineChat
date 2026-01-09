package org.finetree.finechat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.finetree.finechat.chat.ChatFormatter;
import org.finetree.finechat.chat.ChatListener;
import org.finetree.finechat.config.ChatConfig;
import org.finetree.finechat.hooks.GroupManagerHook;
import org.finetree.finechat.hooks.LuckPermsHook;
import org.finetree.finechat.hooks.PlaceholderApiHook;
import org.finetree.finechat.hooks.VaultChatHook;
import org.finetree.finechat.scheduler.PlatformScheduler;
import org.finetree.finechat.scheduler.PlatformSchedulers;
import redempt.redlib.config.ConfigManager;

public class FineChat extends JavaPlugin {

    private PlatformScheduler scheduler;

    private GroupManagerHook groupManagerHook;

    private final VaultChatHook vaultHook = new VaultChatHook();
    private final LuckPermsHook luckPermsHook = new LuckPermsHook();
    private final PlaceholderApiHook placeholderHook = new PlaceholderApiHook();

    @Override
    public void onEnable() {
        // RedLib config
        ConfigManager.create(this)
                .target(ChatConfig.class)
                .saveDefaults()
                .reload();

        scheduler = PlatformSchedulers.create(this);

        // Hooks (optional)
        groupManagerHook = new GroupManagerHook(this);
        if (groupManagerHook.isPresent()) {
            getLogger().info("[FineChat] Hooked GroupManager");
        }

        vaultHook.setup(this);
        luckPermsHook.setup(this);

        ChatFormatter formatter = new ChatFormatter(groupManagerHook, vaultHook, luckPermsHook, placeholderHook);

        Bukkit.getPluginManager().registerEvents(new ChatListener(scheduler, formatter), this);

        getLogger().info("FineChat enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("FineChat disabled.");
    }

    public PlatformScheduler getPlatformScheduler() {
        return scheduler;
    }
}
