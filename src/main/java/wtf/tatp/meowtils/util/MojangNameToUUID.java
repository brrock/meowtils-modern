package wtf.tatp.meowtils.util;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.HttpJson;

/** Async Mojang profile lookup used by AutoBlacklist/AutoSafelist. */
public final class MojangNameToUUID {
    private MojangNameToUUID() {}

    public static void lookup(String name, Consumer<String> callback) {
        if (name == null || name.isBlank()) {
            callback.accept(null);
            return;
        }
        HttpJson.get(URI.create("https://api.mojang.com/users/profiles/minecraft/" + java.net.URLEncoder.encode(name.trim(), java.nio.charset.StandardCharsets.UTF_8)), Map.of())
                .thenAccept(json -> {
                    String raw = json.has("id") ? json.get("id").getAsString() : "";
                    Minecraft.getInstance().execute(() -> callback.accept(raw.isBlank() ? null : dashed(raw)));
                })
                .exceptionally(error -> {
                    Minecraft.getInstance().execute(() -> callback.accept(null));
                    return null;
                });
    }

    private static String dashed(String raw) {
        String hex = raw.replace("-", "");
        if (hex.length() != 32) return raw;
        return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20);
    }
}
