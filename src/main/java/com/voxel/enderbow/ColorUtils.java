package com.voxel.enderbow;

import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {
    private static final Pattern HEX = Pattern.compile("(?i)#([0-9a-f]{6})");

    public static String color(String input) {
        if (input == null) return null;
        // translate & codes first using legacy hex conversion
        String translated = translateAlternateColorCodes('&', input);
        // replace #rrggbb with a hex color in a way that's compatible with older/newer APIs
        Matcher matcher = HEX.matcher(translated);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement;
            try {
                // try to call ChatColor.of("#rrggbb") via reflection so we can compile against APIs
                // that might not declare the method at compile time
                Class<?> cc = Class.forName("org.bukkit.ChatColor");
                try {
                    java.lang.reflect.Method of = cc.getMethod("of", String.class);
                    Object ccInst = of.invoke(null, "#" + hex);
                    replacement = ccInst.toString();
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                    // ChatColor.of not available at runtime — fall back to legacy hex sequence
                    replacement = hexToLegacy(hex);
                }
            } catch (ClassNotFoundException cnf) {
                // ChatColor class not available (shouldn't happen when compiling against Paper/Spigot),
                // fall back to legacy hex sequence
                replacement = hexToLegacy(hex);
            }
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = '\u00A7';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    private static String hexToLegacy(String hex) {
        // Convert rrggbb -> §x§r§r§g§g§b§b (supported since Minecraft 1.16)
        final char section = '\u00A7';
        StringBuilder sb = new StringBuilder();
        sb.append(section).append('x');
        for (char c : hex.toLowerCase().toCharArray()) {
            sb.append(section).append(c);
        }
        return sb.toString();
    }

    public static List<String> colorList(List<String> input) {
        if (input == null) return List.of();
        return input.stream().map(ColorUtils::color).collect(Collectors.toList());
    }
}
