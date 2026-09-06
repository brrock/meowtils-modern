package wtf.tatp.meowtils;

import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Original 2.0.1 game-directory layout, created on client start and stop. */
public final class MeowtilsData {
    private static Path override;

    private MeowtilsData() {}

    /** Tests and {@code ConfigManager.initialize} pin the game dir before Fabric is ready. */
    public static void use(Path gameDir) {
        override = gameDir == null ? null : gameDir.resolve("meowtils");
    }

    public static Path root() {
        if (override != null) return override;
        try {
            return FabricLoader.getInstance().getGameDir().resolve("meowtils");
        } catch (Exception ignored) {
            return Path.of(System.getProperty("java.io.tmpdir", "."), "meowtils-test");
        }
    }
    public static Path configFile() { return root().resolve("config.json"); }
    public static Path logFile() { return root().resolve("meowtils.log"); }
    public static Path extensions() { return root().resolve("extensions"); }
    public static Path chatFilters() { return root().resolve("chatfilters"); }
    public static Path defaultChatFilter() { return chatFilters().resolve("default.txt"); }
    public static Path customCape() { return root().resolve("custom_cape"); }
    public static Path customSkins() { return root().resolve("custom_skins"); }
    public static Path items() { return root().resolve("items"); }
    public static Path autoUpdate() { return root().resolve("auto_update"); }
    public static Path safelist() { return root().resolve("meowtilssafelist.json"); }
    public static Path blacklist() { return root().resolve("meowtilsblacklist.json"); }
    public static Path friendlist() { return root().resolve("meowtilsfriendlist.json"); }
    public static Path urchinTags() { return root().resolve("urchintags.json"); }
    public static Path nickbotList() { return root().resolve("nickbot_list.json"); }
    public static Path autoGgList() { return root().resolve("autogg_list.json"); }
    public static Path autoGlList() { return root().resolve("autogl_list.json"); }
    public static Path itemHighlightBlacklist() { return items().resolve("itemhighlightblacklist.json"); }
    public static Path itemHighlightSafelist() { return items().resolve("itemhighlightsafelist.json"); }

    /** Older port names that should still be read if the original file is missing. */
    public static Path legacyCapes() { return root().resolve("capes"); }
    public static Path legacySafelist() { return root().resolve("safelist.json"); }
    public static Path legacyBlacklist() { return root().resolve("blacklist.json"); }
    public static Path legacyAutoGg() { return root().resolve("autogg.json"); }
    public static Path legacyAutoGl() { return root().resolve("autogl.json"); }
    public static Path legacyItemBlacklist() { return root().resolve("itemhighlight_blacklist.json"); }
    public static Path legacyItemSafelist() { return root().resolve("itemhighlight_safelist.json"); }

    public static Path firstExisting(Path preferred, Path... fallbacks) {
        if (Files.isRegularFile(preferred)) return preferred;
        for (Path fallback : fallbacks) if (Files.isRegularFile(fallback)) return fallback;
        return preferred;
    }

    public static Path ensure() {
        Path home = root();
        try {
            Files.createDirectories(home);
            Files.createDirectories(extensions());
            Files.createDirectories(autoUpdate());
            Files.createDirectories(customCape());
            Files.createDirectories(customSkins());
            Files.createDirectories(items());
            Files.createDirectories(chatFilters());
            touch(configFile(), "{}");
            touch(logFile(), "");
            touch(safelist(), "{}");
            touch(blacklist(), "{}");
            touch(friendlist(), "{}");
            touch(urchinTags(), "{}");
            touch(itemHighlightBlacklist(), "[]");
            touch(itemHighlightSafelist(), "[]");
            touch(nickbotList(), "[]");
            touch(autoGgList(), "[]");
            touch(autoGlList(), "[]");
            if (!Files.isRegularFile(defaultChatFilter())) {
                Files.writeString(defaultChatFilter(), """
                        # Meowtils chat filter
                        ?You are still radiating with Generosity!
                        ?Your game was boosted by
                        <You tipped
                        """);
            }
        } catch (Exception exception) {
            System.err.println("Meowtils: unable to create " + home + ": " + exception);
        }
        return home;
    }

    private static void touch(Path file, String empty) throws Exception {
        if (!Files.exists(file)) Files.writeString(file, empty);
    }
}
