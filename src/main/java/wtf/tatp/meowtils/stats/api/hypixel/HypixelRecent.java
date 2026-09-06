package wtf.tatp.meowtils.stats.api.hypixel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.StatsUuid;
import wtf.tatp.meowtils.stats.util.StatsUtil;

public final class HypixelRecent implements StatsSource {
    @Override
    public String getId() {
        return "hypixel-recent";
    }

    @Override
    public CompletableFuture<StatsContainer> fetch(String uuid) {
        return StatsUuid.resolve(uuid).thenCompose(resolved -> {
            if (resolved == null) return CompletableFuture.completedFuture(null);
            return HypixelApi.get("https://api.hypixel.net/v2/recentgames?uuid=" + StatsUuid.encode(resolved))
                    .thenApply(json -> parse(json, resolved));
        });
    }

    private static StatsContainer parse(JsonObject json, String uuid) {
        if (!HypixelApi.ok(json, null)) return null;
        List<JsonObject> games = new ArrayList<>();
        if (json.has("games") && json.get("games").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("games")) {
                if (element.isJsonObject()) games.add(element.getAsJsonObject());
            }
        }
        games.sort((a, b) -> Long.compare(StatsUtil.getLong(b, "date", 0L), StatsUtil.getLong(a, "date", 0L)));
        List<StatsContainer.RecentGames> recent = new ArrayList<>();
        int limit = Math.min(3, games.size());
        for (int i = 0; i < limit; i++) {
            JsonObject game = games.get(i);
            recent.add(new StatsContainer.RecentGames(
                    StatsUtil.getLong(game, "date", 0L),
                    StatsUtil.getString(game, "gameType", "Unknown"),
                    StatsUtil.getString(game, "mode", "Unknown"),
                    StatsUtil.getString(game, "map", "Unknown"),
                    StatsUtil.getLong(game, "ended", 0L)));
        }
        StatsContainer container = new StatsContainer();
        container.updateRecent(recent);
        Meowtils.info("(Hypixel API) Fetched stats for " + uuid);
        return container;
    }
}
