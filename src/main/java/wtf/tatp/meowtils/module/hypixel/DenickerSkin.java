package wtf.tatp.meowtils.module.hypixel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

public final class DenickerSkin {
    private DenickerSkin() {}

    public record Data(String hash, String profileName) {}

    public static Data fromPlayerInfo(PlayerInfo info) {
        if (info == null) return null;
        Data fromProperties = fromTexturesProperty(info.getProfile());
        if (fromProperties != null) return fromProperties;
        return fromPlayerSkin(info.getSkin());
    }

    public static Data fromTexturesProperty(GameProfile profile) {
        if (profile == null) return null;
        var textures = profile.properties().get("textures");
        Property property = textures == null || textures.isEmpty() ? null : textures.iterator().next();
        if (property == null) return null;
        return fromTexturesBase64(property.value());
    }

    public static Data fromTexturesBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(decoded).getAsJsonObject();
            if (!obj.has("textures")) return null;
            JsonObject textures = obj.getAsJsonObject("textures");
            if (!textures.has("SKIN")) return null;
            JsonObject skin = textures.getAsJsonObject("SKIN");
            if (!skin.has("url")) return null;
            String hash = hashFromUrl(skin.get("url").getAsString());
            if (hash == null) return null;
            String profileName = obj.has("profileName") ? obj.get("profileName").getAsString() : "";
            return new Data(hash, profileName);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Data fromPlayerSkin(PlayerSkin skin) {
        if (skin == null) return null;
        String hash = hashFromTexture(skin.body());
        if (hash == null) return null;
        return new Data(hash, "");
    }

    public static String hashFromTexture(ClientAsset.Texture texture) {
        if (texture == null) return null;
        if (texture instanceof ClientAsset.DownloadedTexture downloaded) {
            return hashFromUrl(downloaded.url());
        }
        return hashFromUrl(texture.texturePath());
    }

    public static String hashFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        int slash = url.lastIndexOf('/');
        if (slash < 0 || slash >= url.length() - 1) return null;
        String hash = url.substring(slash + 1);
        return hash.isEmpty() ? null : hash;
    }

    public static String hashFromUrl(Identifier identifier) {
        if (identifier == null) return null;
        return hashFromUrl(identifier.toString());
    }
}
