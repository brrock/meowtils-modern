package wtf.tatp.meowtils.util;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class PlayerUtil {
    private PlayerUtil() {}

    public static boolean isNicked(GameProfile profile) {
        if (profile == null) return false;
        UUID uuid = profile.id();
        return uuid != null && uuid.version() == 1;
    }

    public static GameProfile getProfile(String name) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || name == null) return null;
        PlayerInfo info = connection.getPlayerInfoIgnoreCase(name);
        return info == null ? null : info.getProfile();
    }
}
