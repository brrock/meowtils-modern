package wtf.tatp.meowtils.stats.api.hypixel;

import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.StatsUuid;
import wtf.tatp.meowtils.stats.util.StatsUtil;

public final class HypixelStatus implements StatsSource {
    @Override
    public String getId() {
        return "hypixel-status";
    }

    @Override
    public CompletableFuture<StatsContainer> fetch(String uuid) {
        return StatsUuid.resolve(uuid).thenCompose(resolved -> {
            if (resolved == null) return CompletableFuture.completedFuture(null);
            return HypixelApi.get("https://api.hypixel.net/v2/status?uuid=" + StatsUuid.encode(resolved))
                    .thenApply(json -> parse(json, resolved));
        });
    }

    private static StatsContainer parse(JsonObject json, String uuid) {
        if (!HypixelApi.ok(json, "session")) return null;
        JsonObject session = json.getAsJsonObject("session");
        StatsContainer container = new StatsContainer();
        container.updateStatus(new StatsContainer.OnlineStatus(
                StatsUtil.getBoolean(session, "online", false),
                StatsUtil.getString(session, "gameType", "Unknown"),
                StatsUtil.getString(session, "mode", "Unknown"),
                StatsUtil.getString(session, "map", "Unknown")));
        Meowtils.info("(Hypixel API) Fetched status for " + uuid);
        return container;
    }
}
