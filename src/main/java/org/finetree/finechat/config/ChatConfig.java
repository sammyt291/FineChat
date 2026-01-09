package org.finetree.finechat.config;

import redempt.redlib.config.annotations.Comment;

/**
 * RedLib ConfigManager will load/save these static fields to config.yml.
 * Keep everything public for quick access (as requested).
 */
public class ChatConfig {

    @Comment("")
    @Comment("==============================")
    @Comment("======== MAIN OPTIONS ========")
    @Comment("==============================")


    // master toggle
    @Comment("")
    @Comment("Should this plugin run?")
    public static boolean enabled = true;


    // if true, we cancel AsyncPlayerChatEvent and handle sending ourselves
    @Comment("")
    @Comment("Override Vanilla Chat system?")
    public static boolean overrideVanillaChat = true;

    // Format supports PlaceholderAPI placeholders and color tags.
    @Comment("")
    @Comment("Chat format, Supports PAPI")
    @Comment("FineTowns specific placeholders: {prefix} {suffix} {player} {displayname} {message} {world}")
    @Comment("The above prefixes work without PAPI installed.")
    public static String format = "{prefix}{displayname}&7: &f{message}";

    // If Vault chat is present, use it first for prefix/suffix
    @Comment("")
    @Comment("Should Vault be prioritised over LP or GM?")
    public static boolean preferVaultChat = true;

    // If true, we will also include LuckPerms meta or GroupManager prefix/suffix as fallback
    @Comment("")
    @Comment("Should LP or GM be used directly if Vault Prefix is empty?")
    public static boolean useLuckPermsGroupManager = true;

    // If true, the player's chat message will run through PlaceholderAPI too.
    @Comment("")
    @Comment("Enable PlaceholderAPI Processing?")
    public static boolean papiOnMessage = true;

    // Permission node that allows using color codes (& / §) in chat messages
    @Comment("")
    @Comment("Allow Colour codes permission")
    public static String permChatColor = "finechat.color";

    // Permission node that allows using special tags (<rainbow>, <gradient>, <hex>)
    public static String permChatSpecialColor = "finechat.color.special";

    // If true, strip color codes from player message if they lack permission
    public static boolean stripColorsIfNoPerm = true;

    // If true, allow § color character in config/messages (in addition to &)
    public static boolean allowSectionSymbol = true;

    // Rainbow/gradient default behavior if no closing tag is used
    public static boolean tagsApplyToRestOfString = true;
}
