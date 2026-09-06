package wtf.tatp.meowtils.stats.api.hypixel;

import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.StatsUuid;
import wtf.tatp.meowtils.stats.util.StatsUtil;

public final class HypixelPlayer implements StatsSource {
    @Override
    public String getId() {
        return "hypixel-player";
    }

    @Override
    public CompletableFuture<StatsContainer> fetch(String name) {
        return StatsUuid.resolve(name).thenCompose(uuid -> {
            if (uuid == null) return CompletableFuture.completedFuture(null);
            return HypixelApi.get("https://api.hypixel.net/v2/player?uuid=" + StatsUuid.encode(uuid))
                    .thenApply(json -> parse(json, name));
        });
    }

    private static StatsContainer parse(JsonObject json, String name) {
        if (!HypixelApi.ok(json, "player")) return null;
        StatsContainer container = StatsUtil.parsePlayerData(json.getAsJsonObject("player"));
        Meowtils.info("(Hypixel API) Fetched stats for " + name);
        return container;
    }
}
