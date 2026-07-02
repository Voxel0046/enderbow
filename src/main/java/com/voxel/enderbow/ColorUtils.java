package com.voxel.enderbow;

import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public final class ColorUtils {
    private static final Pattern HEX = Pattern.compile("(?i)#([0-9a-f]{6})");

    public static String color(String input) {
        if (input == null) return null;
        // translate & codes first
        String translated = ChatColor.translateAlternateColorCodes('&', input);
        // replace #rrggbb with ChatColor.of("#rrggbb") if supported
        Matcher matcher = HEX.matcher(translated);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> colorList(List<String> input) {
        if (input == null) return List.of();
        return input.stream().map(ColorUtils::color).collect(Collectors.toList());
    }
}
