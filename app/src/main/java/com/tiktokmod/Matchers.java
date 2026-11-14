package com.tiktokmod;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conservative identifiers only: unknown or obfuscated views are deliberately ignored. */
final class Matchers {
    private static final Set<String> BIO = new HashSet<>(Arrays.asList(
            "profile_bio", "user_bio", "bio_description", "user_signature", "profile_signature"));
    private static final Set<String> COMMENT = new HashSet<>(Arrays.asList(
            "comment_text", "comment_content", "comment_description"));
    private static final Set<String> LINK = new HashSet<>(Arrays.asList(
            "profile_link", "profile_website", "bio_link", "user_website"));
    private static final Set<String> SETTINGS = new HashSet<>(Arrays.asList(
            "ajustes y privacidad", "configuración y privacidad", "settings and privacy"));
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);

    private Matchers() { }
    static boolean isBio(String id) { return BIO.contains(id); }
    static boolean isComment(String id) { return COMMENT.contains(id); }
    static boolean isLink(String id) { return LINK.contains(id); }
    static boolean isSettingsTitle(String text) {
        return text != null && SETTINGS.contains(text.trim().toLowerCase(Locale.ROOT));
    }
    static String webUrl(String text) {
        if (text == null) return null;
        Matcher matcher = URL.matcher(text);
        if (!matcher.find()) return null;
        String url = matcher.group().replaceFirst("[.,!?;:]+$", "");
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null || uri.getUserInfo() != null) return null;
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) return null;
            return url;
        } catch (Exception ignored) { return null; }
    }
}
