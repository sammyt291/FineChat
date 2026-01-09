package org.finetree.finechat.util;

import org.bukkit.ChatColor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports:
 *  - legacy codes with '&' and '§'
 *  - <hex,#RRGGBB> or <hex,RRGGBB> ... </hex> (or applies to end)
 *  - <rainbow> ... </rainbow>
 *  - <gradient,#from,#to> ... </gradient>
 *
 * Not a full MiniMessage replacement - intentionally small & dependency-free.
 */
public final class ColorUtil {

    private static final Pattern HEX_TAG = Pattern.compile("(?i)<hex\\s*,\\s*#?([0-9a-f]{6})\\s*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADIENT_TAG = Pattern.compile("(?i)<gradient\\s*,\\s*#?([0-9a-f]{6})\\s*,\\s*#?([0-9a-f]{6})\\s*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAINBOW_TAG = Pattern.compile("(?i)<rainbow\\s*>", Pattern.CASE_INSENSITIVE);

    private static final Pattern CLOSE_HEX = Pattern.compile("(?i)</hex\\s*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSE_GRADIENT = Pattern.compile("(?i)</gradient\\s*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSE_RAINBOW = Pattern.compile("(?i)</rainbow\\s*>", Pattern.CASE_INSENSITIVE);

    private ColorUtil() {}

    public static String colorize(String input, boolean translateAmpersand) {
        if (input == null) return "";

        String out = input;

        // Process advanced tags first
        out = applyHex(out);
        out = applyGradient(out);
        out = applyRainbow(out);

        // Translate & -> §
        if (translateAmpersand) {
            out = ChatColor.translateAlternateColorCodes('&', out);
        }
        return out;
    }

    public static String stripColors(String input) {
        if (input == null) return "";
        return ChatColor.stripColor(input);
    }

    private static String applyHex(String in) {
        String out = in;
        while (true) {
            Matcher m = HEX_TAG.matcher(out);
            if (!m.find()) break;

            String hex = m.group(1);
            int start = m.start();
            int afterOpen = m.end();

            int closeIndex = indexOfRegex(out, CLOSE_HEX, afterOpen);
            String inner;
            int endIndex;
            if (closeIndex >= 0) {
                inner = out.substring(afterOpen, closeIndex);
                endIndex = closeIndex + matchLength(out, CLOSE_HEX, closeIndex);
            } else {
                inner = out.substring(afterOpen);
                endIndex = out.length();
            }

            String colored = legacyHex(hex) + inner;
            out = out.substring(0, start) + colored + out.substring(endIndex);
        }
        // Remove any stray closing tags
        out = CLOSE_HEX.matcher(out).replaceAll("");
        return out;
    }

    private static String applyGradient(String in) {
        String out = in;
        while (true) {
            Matcher m = GRADIENT_TAG.matcher(out);
            if (!m.find()) break;

            Color from = Color.decode("#" + m.group(1));
            Color to = Color.decode("#" + m.group(2));
            int start = m.start();
            int afterOpen = m.end();

            int closeIndex = indexOfRegex(out, CLOSE_GRADIENT, afterOpen);
            String inner;
            int endIndex;
            if (closeIndex >= 0) {
                inner = out.substring(afterOpen, closeIndex);
                endIndex = closeIndex + matchLength(out, CLOSE_GRADIENT, closeIndex);
            } else {
                inner = out.substring(afterOpen);
                endIndex = out.length();
            }

            String colored = applyGradientToText(inner, from, to);
            out = out.substring(0, start) + colored + out.substring(endIndex);
        }
        out = CLOSE_GRADIENT.matcher(out).replaceAll("");
        return out;
    }

    private static String applyRainbow(String in) {
        String out = in;
        while (true) {
            Matcher m = RAINBOW_TAG.matcher(out);
            if (!m.find()) break;

            int start = m.start();
            int afterOpen = m.end();

            int closeIndex = indexOfRegex(out, CLOSE_RAINBOW, afterOpen);
            String inner;
            int endIndex;
            if (closeIndex >= 0) {
                inner = out.substring(afterOpen, closeIndex);
                endIndex = closeIndex + matchLength(out, CLOSE_RAINBOW, closeIndex);
            } else {
                inner = out.substring(afterOpen);
                endIndex = out.length();
            }

            String colored = applyRainbowToText(inner);
            out = out.substring(0, start) + colored + out.substring(endIndex);
        }
        out = CLOSE_RAINBOW.matcher(out).replaceAll("");
        return out;
    }

    private static String applyGradientToText(String text, Color from, Color to) {
        List<Integer> indexes = visibleCharIndexes(text);
        int n = indexes.size();
        if (n == 0) return text;

        StringBuilder sb = new StringBuilder(text.length() * 2);
        int visiblePos = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Preserve existing legacy codes and their following char
            if (c == '§' && i + 1 < text.length()) {
                sb.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            }

            if (c == '&' && i + 1 < text.length()) { // keep as-is for later translation
                sb.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            }

            if (isVisibleChar(c)) {
                double t = (n == 1) ? 0 : (visiblePos / (double) (n - 1));
                Color col = lerp(from, to, t);
                sb.append(legacyHex(col));
                sb.append(c);
                visiblePos++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String applyRainbowToText(String text) {
        List<Integer> indexes = visibleCharIndexes(text);
        int n = indexes.size();
        if (n == 0) return text;

        StringBuilder sb = new StringBuilder(text.length() * 2);
        int visiblePos = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                sb.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            }
            if (c == '&' && i + 1 < text.length()) {
                sb.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            }

            if (isVisibleChar(c)) {
                float hue = (n == 1) ? 0f : (visiblePos / (float) n);
                Color col = Color.getHSBColor(hue, 1f, 1f);
                sb.append(legacyHex(col));
                sb.append(c);
                visiblePos++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isVisibleChar(char c) {
        return c != '\n' && c != '\r';
    }

    private static List<Integer> visibleCharIndexes(String text) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                i++;
                continue;
            }
            if (c == '&' && i + 1 < text.length()) {
                i++;
                continue;
            }
            if (isVisibleChar(c)) idx.add(i);
        }
        return idx;
    }

    private static Color lerp(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(clamp(r), clamp(g), clamp(bl));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static String legacyHex(String hex6) {
        Color c = Color.decode("#" + hex6);
        return legacyHex(c);
    }

    public static String legacyHex(Color c) {
        String hex = String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        StringBuilder sb = new StringBuilder("§x");
        for (char ch : hex.toCharArray()) {
            sb.append('§').append(ch);
        }
        return sb.toString();
    }

    private static int indexOfRegex(String text, Pattern pattern, int fromIndex) {
        Matcher m = pattern.matcher(text);
        if (m.find(fromIndex)) return m.start();
        return -1;
    }

    private static int matchLength(String text, Pattern pattern, int startIndex) {
        Matcher m = pattern.matcher(text);
        if (m.find(startIndex) && m.start() == startIndex) {
            return m.end() - m.start();
        }
        return 0;
    }
}
