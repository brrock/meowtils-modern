package wtf.tatp.meowtils.manager.lists;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.stats.StatsContainer;

/** Persists Urchin tags to {@code meowtils/urchintags.json}. */
public final class UrchinManager {
    private static final Path FILE = MeowtilsData.urchinTags();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final java.lang.reflect.Type TYPE = new TypeToken<Map<String, Entry>>() {}.getType();
    private static Map<String, Entry> taggedPlayers = new ConcurrentHashMap<>();

    public static final class Entry {
        public String name;
        public String uuid;
        public List<StatsContainer.UrchinTag> tags;
    }

    static {
        loadTags();
    }

    private UrchinManager() {}

    public static void put(String name, String uuid, List<StatsContainer.UrchinTag> tags) {
        if (name == null || name.isEmpty()) return;
        Entry entry = new Entry();
        entry.name = name;
        entry.uuid = uuid;
        entry.tags = tags;
        taggedPlayers.entrySet().removeIf(e -> uuid != null && !uuid.isEmpty() && uuid.equalsIgnoreCase(e.getValue().uuid));
        taggedPlayers.put(name.toLowerCase(), entry);
        saveTags();
    }

    public static void remove(String name, String uuid) {
        if (name != null && !name.isEmpty()) taggedPlayers.remove(name.toLowerCase());
        if (uuid != null && !uuid.isEmpty()) {
            taggedPlayers.entrySet().removeIf(e -> uuid.equalsIgnoreCase(e.getValue().uuid));
        }
        saveTags();
    }

    public static Entry get(String name) {
        if (name == null || name.isEmpty()) return null;
        return taggedPlayers.get(name.toLowerCase());
    }

    private static void loadTags() {
        try {
            if (!Files.isRegularFile(FILE)) return;
            String text = Files.readString(FILE).trim();
            if (text.isEmpty() || "{}".equals(text)) return;
            Map<String, Entry> loaded = GSON.fromJson(text, TYPE);
            if (loaded != null) taggedPlayers = new ConcurrentHashMap<>(loaded);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void saveTags() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(taggedPlayers));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
