package com.astrotool.hook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read-only helper for identifying shared-library names mentioned in
 * MemoryPatch::createWithHex source lines. It does not load libraries,
 * inspect process memory, or apply patches.
 */
public final class LibraryTargetScanner {
    private static final Pattern CREATE_WITH_HEX = Pattern.compile(
            "MemoryPatch\\s*::\\s*createWithHex\\s*\\(\\s*[\"']([^\"']+\\.so)[\"']",
            Pattern.MULTILINE
    );

    private LibraryTargetScanner() {
        // Utility class.
    }

    /**
     * Returns each distinct .so target in first-seen order.
     *
     * @param source source text to inspect; null is treated as empty
     * @return immutable list of detected library filenames
     */
    public static List<String> scan(String source) {
        if (source == null || source.length() == 0) {
            return Collections.emptyList();
        }

        Set<String> libraries = new LinkedHashSet<>();
        Matcher matcher = CREATE_WITH_HEX.matcher(source);

        while (matcher.find()) {
            String library = matcher.group(1).trim();
            if (isLibraryFilename(library)) {
                libraries.add(library);
            }
        }

        return Collections.unmodifiableList(new ArrayList<>(libraries));
    }

    private static boolean isLibraryFilename(String value) {
        if (value.length() <= 3 || value.indexOf('/') >= 0
                || value.indexOf('\\') >= 0 || value.indexOf(' ') >= 0) {
            return false;
        }
        return value.endsWith(".so");
    }

    /**
     * Convenience display value for a simple single-target scan.
     */
    public static String displayTargets(String source) {
        List<String> targets = scan(source);
        if (targets.isEmpty()) {
            return "Unknown";
        }

        StringBuilder result = new StringBuilder();
        for (String target : targets) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(target);
        }
        return result.toString();
    }
}
