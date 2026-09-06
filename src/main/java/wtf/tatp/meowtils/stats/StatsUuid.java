package wtf.tatp.meowtils.stats;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import wtf.tatp.meowtils.util.MojangNameToUUID;

/** Resolves a player name or already-dashed UUID without blocking the caller. */
public final class StatsUuid {
    private static final Pattern UUID = Pattern.compile("(?i)[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}");

    private StatsUuid() {}

    public static CompletableFuture<String> resolve(String nameOrUuid) {
        if (nameOrUuid == null || nameOrUuid.isBlank()) return CompletableFuture.completedFuture(null);
        String trimmed = nameOrUuid.trim();
        if (UUID.matcher(trimmed).matches()) return CompletableFuture.completedFuture(dashed(trimmed));
        CompletableFuture<String> done = new CompletableFuture<>();
        MojangNameToUUID.lookup(trimmed, done::complete);
        return done;
    }

    public static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String dashed(String raw) {
        String hex = raw.replace("-", "");
        if (hex.length() != 32) return raw;
        return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20);
    }
}
