package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.manager.lists.SafelistManager;
import wtf.tatp.meowtils.module.meowtils.Icons;
import wtf.tatp.meowtils.util.Settings;

public final class BlacklistIcon implements IconProvider {
    private static final String ICON = ChatFormatting.BOLD + "⚠ " + ChatFormatting.RESET;
    private static final String LIGHT_PURPLE_ICON = ChatFormatting.LIGHT_PURPLE.toString() + ChatFormatting.BOLD + "⚠ " + ChatFormatting.RESET;

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        Icons icons = Module.get(Icons.class);
        if (icons == null || !Settings.bool(icons, "blacklistIcon", true)) return "";
        if (nametag && !Icons.displayInNametag()) return "";
        if (tablist && !Icons.displayInTab()) return "";
        if (profile == null || profile.id() == null || profile.name() == null) return "";
        boolean blacklisted = BlacklistManager.isBlacklisted(profile.id().toString()) || BlacklistManager.isBlacklisted(profile.name());
        boolean safelisted = SafelistManager.isSafelisted(profile.id().toString()) || SafelistManager.isSafelisted(profile.name());
        String entry = BlacklistManager.getEntry(profile.id().toString());
        if (entry == null) entry = BlacklistManager.getEntry(profile.name());
        ChatFormatting color = BlacklistManager.getReasonColor(entry);
        if (!blacklisted) return "";
        return safelisted ? LIGHT_PURPLE_ICON : color.toString() + ICON;
    }
}
