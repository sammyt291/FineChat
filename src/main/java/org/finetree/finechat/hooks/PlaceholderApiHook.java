package org.finetree.finechat.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderApiHook {

    // Matches tokens like %something_here% (works even when adjacent: %a%%b%)
    private static final Pattern PLACEHOLDER_TOKEN = Pattern.compile("%[^%]+%");

    public boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Replace PAPI placeholders token-by-token, supporting adjacent placeholders like:
     * %taghere%%2ndtaghere%
     *
     * Also runs multiple passes because some expansions may output new placeholders.
     */
    public String apply(Player player, String input) {
        if (input == null) return "";
        if (player == null) return input;
        if (!isHooked()) return input;

        String out = input;

        // A couple passes handles nested/returned placeholders without risking infinite loops
        for (int pass = 0; pass < 3; pass++) {
            Matcher m = PLACEHOLDER_TOKEN.matcher(out);
            if (!m.find()) break;

            m.reset();
            StringBuffer sb = new StringBuffer(out.length());

            while (m.find()) {
                String token = m.group(); // e.g. "%player_name%"
                String replaced;
                try {
                    // Replace JUST this token
                    replaced = PlaceholderAPI.setPlaceholders(player, token);
                    Bukkit.getServer().getLogger().info(token + " " + replaced);
                } catch (Throwable t) {
                    replaced = token;
                }
                if (replaced == null) replaced = token;

                m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
            }

            m.appendTail(sb);
            String next = sb.toString();

            // Stabilized
            if (next.equals(out)) break;
            out = next;
        }

        return out;
    }
}
