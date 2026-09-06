package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.skywars.SkywarsAlerts;
import wtf.tatp.meowtils.util.Settings;

public final class SkywarsIcon implements IconProvider {
    private static final String DIAMOND_SWORD = "§b§l⚔§r";
    private static final String FIRE_SWORD = "§c§l⚔§r";
    private static final String ENDER_PEARL = "§d§l❃§r";
    private static final String STRENGTH_POTION = "§4§l⚒§r";
    private static final String KNOCKBACK_SWORD = "§e§l⚡§r";
    private static final String KNOCKBACK_ROD = "§6§l⚡§r";

    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        SkywarsAlerts module = Module.get(SkywarsAlerts.class);
        if (module == null || !module.getState() || Skywars.GAME.isNotActive() || !Settings.bool(module, "nametagIcon", true)) return "";
        var uuid = profile.id();
        String sword = Settings.bool(module, "swordIcon", true)
                ? (SkywarsAlerts.heldItem(uuid, "diamond_sword") ? DIAMOND_SWORD : SkywarsAlerts.heldItem(uuid, "iron_sword") ? FIRE_SWORD : "")
                : "";
        String knockback = Settings.bool(module, "knockbackIcon", true)
                ? (SkywarsAlerts.heldItem(uuid, "fishing_rod") ? KNOCKBACK_ROD : SkywarsAlerts.heldItem(uuid, "golden_sword") ? KNOCKBACK_SWORD : "")
                : "";
        String pearl = Settings.bool(module, "pearlIcon", true) && SkywarsAlerts.heldItem(uuid, "ender_pearl") ? ENDER_PEARL : "";
        String strength = Settings.bool(module, "strengthIcon", true) && SkywarsAlerts.heldItem(uuid, "potion") ? STRENGTH_POTION : "";
        return " " + sword + knockback + pearl + strength;
    }
}
