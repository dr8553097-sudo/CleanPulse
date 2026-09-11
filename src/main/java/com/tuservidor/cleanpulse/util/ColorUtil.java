package com.tuservidor.cleanpulse.util;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern TAG_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private ColorUtil() {}

    public static String color(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);
        String text = sb.toString();

        Matcher tagMatcher = TAG_HEX_PATTERN.matcher(text);
        StringBuilder tagSb = new StringBuilder();
        while (tagMatcher.find()) {
            String hex = tagMatcher.group(1);
            tagMatcher.appendReplacement(tagSb, ChatColor.of("#" + hex).toString());
        }
        tagMatcher.appendTail(tagSb);

        return ChatColor.translateAlternateColorCodes('&', tagSb.toString());
    }
}
