package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.lists.FriendlistManager;
import wtf.tatp.meowtils.module.meowtils.Icons;
import wtf.tatp.meowtils.util.Settings;

public final class FriendlistIcon implements IconProvider {
    private static final String ICON = ChatFormatting.GOLD + "✮ " + ChatFormatting.RESET;

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        Icons icons = Module.get(Icons.class);
        if (icons == null || !Settings.bool(icons, "friendIcon", true)) return "";
        if (nametag && !Icons.displayInNametag()) return "";
        if (tablist && !Icons.displayInTab()) return "";
        if (profile == null || profile.id() == null || profile.name() == null) return "";
        boolean friendlisted = FriendlistManager.isFriendlisted(profile.id().toString())
                || FriendlistManager.isFriendlisted(profile.name());
        return friendlisted ? ICON : "";
    }
}
