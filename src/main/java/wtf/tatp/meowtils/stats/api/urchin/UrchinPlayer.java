package wtf.tatp.meowtils.stats.api.urchin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.extension.HttpJson;
import wtf.tatp.meowtils.manager.lists.UrchinManager;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.StatsUuid;
import wtf.tatp.meowtils.stats.util.StatsUtil;

public final class UrchinPlayer implements StatsSource {
    @Override
    public String getId() {
        return "urchin-player";
    }

    @Override
    public CompletableFuture<StatsContainer> fetch(String name) {
        String key = Stats.urchinKey();
        if (key == null || key.isEmpty()) {
            Stats.logMissingUrchinKey();
            return CompletableFuture.completedFuture(null);
        }
        String url = "https://api.urchin.gg/v3/player/tags?player=" + StatsUuid.encode(name) + "&key=" + StatsUuid.encode(key.replace(" ", ""));
        return HttpJson.get(URI.create(url), Map.of("User-Agent", "Mozilla/5.0"))
                .thenApply(json -> parse(json, name))
                .exceptionally(error -> {
                    Meowtils.error("(Urchin) Fetching blacklist failed: " + error.getMessage());
                    return null;
                });
    }

    private static StatsContainer parse(JsonObject json, String name) {
        if (json == null) return null;
        String uuid = StatsUtil.getString(json, "uuid", "");
        JsonArray tagsArray = json.has("tags") && json.get("tags").isJsonArray() ? json.getAsJsonArray("tags") : new JsonArray();
        List<StatsContainer.UrchinTag> tags = new ArrayList<>();
        for (JsonElement element : tagsArray) {
            if (!element.isJsonObject()) continue;
            JsonObject tag = element.getAsJsonObject();
            tags.add(new StatsContainer.UrchinTag(
                    StatsUtil.getString(tag, "type", "unknown"),
                    StatsUtil.getString(tag, "reason", ""),
                    StatsUtil.getString(tag, "added_on", "")));
        }
        if (tags.isEmpty()) UrchinManager.remove(name, uuid);
        else UrchinManager.put(name, uuid, tags);
        StatsContainer container = new StatsContainer();
        container.updateUrchinTags(tags);
        Meowtils.info("(Urchin) Fetched blacklist for: " + name);
        return container;
    }
}
