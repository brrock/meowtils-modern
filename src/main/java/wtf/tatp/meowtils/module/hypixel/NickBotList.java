package wtf.tatp.meowtils.module.hypixel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.LinkedHashSet;
import java.util.Set;

/** Accepts original {@code []} lists and empty {@code {}} files created by {@code MeowtilsData.touch}. */
public final class NickBotList {
    private NickBotList() {}

    public static Set<String> parse(String text) {
        Set<String> names = new LinkedHashSet<>();
        if (text == null) return names;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return names;
        JsonElement json = JsonParser.parseString(trimmed);
        if (json == null || json.isJsonNull()) return names;
        if (json.isJsonArray()) {
            addAll(names, json.getAsJsonArray());
            return names;
        }
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.has("names") && object.get("names").isJsonArray()) {
                addAll(names, object.getAsJsonArray("names"));
            }
        }
        return names;
    }

    private static void addAll(Set<String> names, JsonArray array) {
        for (JsonElement element : array) {
            if (element != null && element.isJsonPrimitive()) names.add(element.getAsString());
        }
    }
}
