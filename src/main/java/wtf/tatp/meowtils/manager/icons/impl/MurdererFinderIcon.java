package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.session.MurderMystery;
import wtf.tatp.meowtils.module.hypixel.MurdererFinder;
import wtf.tatp.meowtils.util.Settings;

public final class MurdererFinderIcon implements IconProvider {
    private static final String MURDERER_ICON = ChatFormatting.DARK_RED.toString() + ChatFormatting.BOLD + " !!" + ChatFormatting.RESET;
    private static final String DETECTIVE_ICON = ChatFormatting.GOLD + " ➹" + ChatFormatting.RESET.toString();

    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        MurdererFinder finder = Module.get(MurdererFinder.class);
        if (finder == null || !finder.getState() || !Settings.bool(finder, "nameIcons", true) || MurderMystery.ALL.isNotActive()) {
            return "";
        }
        UUID uuid = profile.id();
        if (MurdererFinder.isMurderer(uuid)) return MURDERER_ICON;
        if (MurdererFinder.isDetective(uuid)) return DETECTIVE_ICON;
        return "";
    }
}
