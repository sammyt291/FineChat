# FineChat

A lightweight, modern chat formatting plugin for Spigot, Paper, and Folia servers.

## Features

- ✨ **Customizable Chat Format** - Full control over how chat messages appear
- 🎨 **Rich Color Support** - Legacy codes, hex colors, gradients, and rainbow text
- 🔗 **Plugin Integrations** - Vault, LuckPerms, GroupManager, and PlaceholderAPI
- ⚡ **Folia Compatible** - Full support for Folia's regionized multithreading
- 🛠️ **Developer API** - Easy-to-use API for other plugins to hook into
- 📦 **Lightweight** - Minimal dependencies, shaded into a single JAR

## Requirements

- Java 17+
- Spigot, Paper, or Folia 1.16.x - 1.21.x

## Installation

1. Download `FineChat-x.x.x.jar` from [Releases](../../releases)
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure `plugins/FineChat/config.yml` to your liking

## Configuration

```yaml
# ==============================
# ======== MAIN OPTIONS ========
# ==============================

# Should this plugin run?
enabled: true

# Override Vanilla Chat system?
overrideVanillaChat: true

# Chat format, Supports PAPI
# Built-in placeholders: {prefix} {suffix} {player} {displayname} {message} {world}
format: "{prefix}{displayname}&7: &f{message}"

# Should Vault be prioritised over LP or GM?
preferVaultChat: true

# Should LP or GM be used directly if Vault Prefix is empty?
useLuckPermsGroupManager: true

# Enable PlaceholderAPI Processing?
papiOnMessage: true

# Allow Colour codes permission
permChatColor: "finechat.color"
permChatSpecialColor: "finechat.color.special"

# Strip color codes from player message if they lack permission
stripColorsIfNoPerm: true

# Allow § color character in config/messages (in addition to &)
allowSectionSymbol: true

# Rainbow/gradient apply to rest of string if no closing tag
tagsApplyToRestOfString: true
```

## Placeholders

### Built-in Placeholders

These work without any additional plugins:

| Placeholder | Description |
|-------------|-------------|
| `{prefix}` | Player's prefix (from GroupManager/Vault/LuckPerms) |
| `{suffix}` | Player's suffix (from GroupManager/Vault/LuckPerms) |
| `{player}` | Player's name |
| `{displayname}` | Player's display name |
| `{message}` | The chat message |
| `{world}` | Player's current world |

### PlaceholderAPI

If PlaceholderAPI is installed, you can use any PAPI placeholder in your format:

```yaml
format: "{prefix}{displayname} &8[&7%player_ping%ms&8]&7: &f{message}"
```

## Color Codes

### Legacy Colors

Players with the `finechat.color` permission can use:

- `&0-9` - Colors (black, dark blue, etc.)
- `&a-f` - Colors (green, aqua, red, etc.)
- `&k` - Obfuscated
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

### Special Color Tags

Players with the `finechat.color.special` permission can use:

#### Hex Colors
```
<hex,#FF5733>This text is orange!</hex>
<hex,FF5733>Also works without #</hex>
```

#### Gradients
```
<gradient,#FF0000,#0000FF>Red to blue gradient!</gradient>
<gradient,FF5733,33FF57>Orange to green!</gradient>
```

#### Rainbow
```
<rainbow>This text is rainbow colored!</rainbow>
```

> **Note:** If no closing tag is provided, the effect applies to the rest of the message.

## Permissions

| Permission | Description |
|------------|-------------|
| `finechat.color` | Allows using `&` color codes in chat |
| `finechat.color.special` | Allows using `<hex>`, `<gradient>`, `<rainbow>` tags |

## Soft Dependencies

FineChat integrates with these plugins (all optional):

| Plugin | Purpose |
|--------|---------|
| **Vault** | Primary source for prefix/suffix |
| **LuckPerms** | Fallback prefix/suffix via meta |
| **GroupManager** | Alternative permission plugin support |
| **PlaceholderAPI** | Extended placeholder support |

### Priority Order

Prefix/suffix resolution follows this priority:
1. **GroupManager** (if present)
2. **Vault** (if `preferVaultChat: true`)
3. **LuckPerms** (if `useLuckPermsGroupManager: true`)

---

## Developer API

FineChat provides an API for other plugins to integrate with.

### Maven/Gradle

Add FineChat as a `compileOnly` dependency (it's a plugin, not a library).

### Check Availability

```java
if (FineChatAPI.isAvailable()) {
    // FineChat is loaded and ready
}
```

### Format Messages

```java
// Format a message using FineChat's formatter
String formatted = FineChatAPI.formatMessage(player, "Hello world!");

// Get a player's resolved prefix/suffix
String prefix = FineChatAPI.getPrefix(player);
String suffix = FineChatAPI.getSuffix(player);
```

### Listen to Chat Events

Listen to `FineChatFormatEvent` to modify chat before it's sent:

```java
import org.finetree.finechat.api.event.FineChatFormatEvent;

@EventHandler
public void onFineChatFormat(FineChatFormatEvent event) {
    Player player = event.getPlayer();
    
    // Modify prefix
    if (player.hasPermission("vip")) {
        event.setPrefix("&6[VIP] &r" + event.getPrefix());
    }
    
    // Modify the message
    event.setMessage(censorBadWords(event.getMessage()));
    
    // Change the format
    event.setFormat("{prefix} {player} &8» &f{message}");
    
    // Or cancel entirely
    if (shouldMute(player)) {
        event.setCancelled(true);
    }
}
```

### Event Properties

| Method | Description |
|--------|-------------|
| `getPlayer()` | The player sending the message |
| `getMessage()` / `setMessage()` | The raw message content |
| `getPrefix()` / `setPrefix()` | The player's prefix |
| `getSuffix()` / `setSuffix()` | The player's suffix |
| `getFormat()` / `setFormat()` | The format template |
| `setCancelled(true)` | Cancel the message |

> **Note:** `FineChatFormatEvent` is called asynchronously.

---

## Building from Source

```bash
git clone https://github.com/YourUsername/FineChat.git
cd FineChat
./gradlew shadowJar
```

The compiled JAR will be in `build/libs/`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- [GitHub Issues](../../issues) - Bug reports and feature requests
