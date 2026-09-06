package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.manager.lists.SafelistManager;
import wtf.tatp.meowtils.module.meowtils.Icons;
import wtf.tatp.meowtils.util.Settings;

public final class SafelistIcon implements IconProvider {
    private static final String CHECK_ICON = ChatFormatting.GREEN.toString() + ChatFormatting.BOLD + "✓ " + ChatFormatting.RESET;
    private static final String CHECK_ICON_BLUE = ChatFormatting.BLUE.toString() + ChatFormatting.BOLD + "✓ " + ChatFormatting.RESET;

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        Icons icons = Module.get(Icons.class);
        if (icons == null || !Settings.bool(icons, "safelistIcon", true)) return "";
        if (nametag && !Icons.displayInNametag()) return "";
        if (tablist && !Icons.displayInTab()) return "";
        if (profile == null || profile.id() == null || profile.name() == null) return "";
        Minecraft mc = Minecraft.getInstance();
        boolean blacklisted = BlacklistManager.isBlacklisted(profile.id().toString()) || BlacklistManager.isBlacklisted(profile.name());
        boolean safelisted = SafelistManager.isSafelisted(profile.id().toString()) || SafelistManager.isSafelisted(profile.name());
        boolean showIcon = !blacklisted && safelisted;
        if (!showIcon) return "";
        return mc.player != null && profile.id().equals(mc.player.getUUID()) ? CHECK_ICON_BLUE : CHECK_ICON;
    }
}
