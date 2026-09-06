package wtf.tatp.meowtils.stats.api.hypixel;

import com.google.gson.JsonObject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.extension.HttpJson;
import wtf.tatp.meowtils.module.hypixel.Stats;

final class HypixelApi {
    private HypixelApi() {}

    static CompletableFuture<JsonObject> get(String url) {
        String key = Stats.hypixelKey();
        if (key.isEmpty()) {
            Stats.logMissingHypixelKey();
            return CompletableFuture.completedFuture(null);
        }
        return HttpJson.get(URI.create(url), Map.of("API-Key", key, "User-Agent", "Mozilla/5.0"))
                .exceptionally(error -> {
                    Meowtils.error("(Hypixel API) " + message(error));
                    return null;
                });
    }

    static boolean ok(JsonObject json, String missingField) {
        if (json == null) return false;
        if (!json.has("success") || !json.get("success").getAsBoolean()) {
            String cause = json.has("cause") ? json.get("cause").getAsString() : "Unknown reason";
            Meowtils.error("(Hypixel API) error: " + cause);
            return false;
        }
        if (missingField != null && (!json.has(missingField) || json.get(missingField).isJsonNull())) {
            Meowtils.error("(Hypixel API) missing " + missingField + " field");
            return false;
        }
        return true;
    }

    private static String message(Throwable error) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        return cause.getMessage() == null ? "Request failed" : cause.getMessage();
    }
}
