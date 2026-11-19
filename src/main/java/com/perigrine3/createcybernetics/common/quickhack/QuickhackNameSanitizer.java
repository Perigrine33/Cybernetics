package com.perigrine3.createcybernetics.common.quickhack;

import java.util.regex.Pattern;

/** Shared client/server rules for player-visible quickhack names. */
final class QuickhackNameSanitizer {
    static final int MAX_LENGTH = 32;
    // Marks and joining controls preserve accented letters and complex-script spelling.
    private static final Pattern DISALLOWED = Pattern.compile("[^\\p{L}\\p{M}\\p{N}_\\-\\x{200C}\\x{200D}]");
    private static final Pattern UNDERSCORES = Pattern.compile("_+");

    private QuickhackNameSanitizer() {}

    static String sanitize(String name) {
        if (name == null) return "";
        String sanitized = DISALLOWED.matcher(name.trim().replace(' ', '_')).replaceAll("");
        sanitized = UNDERSCORES.matcher(sanitized).replaceAll("_");

        // EditBox and the packet codec limit UTF-16 units. Keep that limit without
        // cutting a supplementary-plane letter's surrogate pair in half.
        int end = Math.min(sanitized.length(), MAX_LENGTH);
        if (end > 0 && end < sanitized.length() && Character.isHighSurrogate(sanitized.charAt(end - 1))) {
            end--;
        }
        return sanitized.substring(0, end);
    }
}
