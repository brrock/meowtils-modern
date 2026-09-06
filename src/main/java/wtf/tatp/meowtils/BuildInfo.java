package wtf.tatp.meowtils;

import java.io.InputStream;
import java.util.Properties;

/** Version and GitHub repo baked in at build time from gradle.properties. */
public final class BuildInfo {
    private static final String VERSION;
    private static final String GITHUB_REPO;

    static {
        Properties properties = new Properties();
        try (InputStream stream = BuildInfo.class.getResourceAsStream("/meowtils-build.properties")) {
            if (stream != null) properties.load(stream);
        } catch (Exception ignored) {
        }
        VERSION = value(properties, "version", "0.0.0");
        GITHUB_REPO = value(properties, "github_repo", "brrock/meowtils-modern");
    }

    private BuildInfo() {}

    public static String version() {
        return VERSION;
    }

    public static String githubRepo() {
        return GITHUB_REPO;
    }

    public static String displayName() {
        return "Meowtils " + VERSION;
    }

    private static String value(Properties properties, String key, String fallback) {
        String raw = properties.getProperty(key, fallback);
        if (raw == null || raw.isBlank() || raw.contains("${")) return fallback;
        return raw.trim();
    }
}
