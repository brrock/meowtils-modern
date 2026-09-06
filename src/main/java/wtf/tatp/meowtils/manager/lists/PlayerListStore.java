package wtf.tatp.meowtils.manager.lists;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import wtf.tatp.meowtils.MeowtilsData;

final class PlayerListStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Prefer the original 2.0.1 filename; copy a legacy file when the original is missing or empty `{}`. */
    static Path resolve(Path preferred, Path... fallbacks) {
        Path source = usable(preferred) ? preferred : firstUsable(fallbacks);
        if (source != null && !source.equals(preferred)) {
            try {
                Files.createDirectories(preferred.getParent());
                if (!usable(preferred)) Files.copy(source, preferred, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}
        }
        return preferred;
    }

    private static Path firstUsable(Path... fallbacks) {
        if (fallbacks == null) return null;
        for (Path fallback : fallbacks) if (usable(fallback)) return fallback;
        return null;
    }

    private static boolean usable(Path file) {
        try {
            if (file == null || !Files.isRegularFile(file)) return false;
            String text = Files.readString(file).trim();
            return !text.isEmpty() && !"{}".equals(text);
        } catch (Exception ignored) { return false; }
    }

    static Map<String, String> loadMap(Path file) {
        try {
            if (!Files.isRegularFile(file)) return new LinkedHashMap<>();
            Map<String, String> parsed = GSON.fromJson(Files.readString(file), new TypeToken<Map<String, String>>(){}.getType());
            return parsed == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parsed);
        } catch (Exception ignored) { return new LinkedHashMap<>(); }
    }

    static Set<String> loadSet(Path file) {
        try {
            if (!Files.isRegularFile(file)) return new LinkedHashSet<>();
            Set<String> parsed = GSON.fromJson(Files.readString(file), new TypeToken<Set<String>>(){}.getType());
            return parsed == null ? new LinkedHashSet<>() : new LinkedHashSet<>(parsed);
        } catch (Exception ignored) { return new LinkedHashSet<>(); }
    }

    static void save(Path file, Object value) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(value));
        } catch (Exception ignored) {}
    }
}
