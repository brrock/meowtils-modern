package wtf.tatp.meowtils.stats.api.abyss;

import com.google.gson.JsonObject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.extension.HttpJson;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.StatsUuid;
import wtf.tatp.meowtils.stats.util.StatsUtil;

public final class AbyssPlayer implements StatsSource {
    @Override
    public String getId() {
        return "abyss-player";
    }

    @Override
    public CompletableFuture<StatsContainer> fetch(String name) {
        return StatsUuid.resolve(name).thenCompose(uuid -> {
            if (uuid == null) return CompletableFuture.completedFuture(null);
            return HttpJson.get(URI.create("http://api.abyssoverlay.com/player?uuid=" + StatsUuid.encode(uuid)),
                            Map.of("User-Agent", "node-ao/2.0.3"))
                    .thenApply(json -> parse(json, name))
                    .exceptionally(error -> {
                        Meowtils.error("Fetching stats from Abyss API failed: " + error.getMessage());
                        return null;
                    });
        });
    }

    private static StatsContainer parse(JsonObject json, String name) {
        if (json == null) return null;
        if (!json.has("success") || !json.get("success").getAsBoolean()) {
            String cause = json.has("cause") ? json.get("cause").getAsString() : "Unknown reason";
            Meowtils.error("(Abyss API) error: " + cause);
            return null;
        }
        if (!json.has("player") || json.get("player").isJsonNull()) {
            Meowtils.error("(Abyss API) missing player field");
            return null;
        }
        StatsContainer container = StatsUtil.parsePlayerData(json.getAsJsonObject("player"));
        Meowtils.info("(Abyss API) Fetched stats for " + name);
        return container;
    }
}
