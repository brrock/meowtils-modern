package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.module.meowtils.Icons;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.util.PlayerUtil;
import wtf.tatp.meowtils.util.Settings;

public final class UrchinIcon implements IconProvider {
    private static final String DARK_RED_SPIKE = ChatFormatting.DARK_RED + "✹ " + ChatFormatting.RESET;
    private static final String RED_SPIKE = ChatFormatting.RED + "✹ " + ChatFormatting.RESET;
    private static final String PURPLE_SPIKE = ChatFormatting.DARK_PURPLE + "✹ " + ChatFormatting.RESET;
    private static final String GRAY_SPIKE = ChatFormatting.GRAY + "✹ " + ChatFormatting.RESET;
    private static final String DARK_GRAY_SPIKE = ChatFormatting.DARK_GRAY + "✹ " + ChatFormatting.RESET;
    private static final String YELLOW_STAR = ChatFormatting.YELLOW + "✴ " + ChatFormatting.RESET;
    private static final String YELLOW_INFO = ChatFormatting.YELLOW + "ⓘ " + ChatFormatting.RESET;

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        Stats stats = Module.get(Stats.class);
        if (stats == null || !stats.getState() || !Settings.bool(stats, "urchinIcon", stats.urchinIcon) || !Settings.bool(stats, "urchinApi", stats.urchinApi)) {
            return "";
        }
        if (nametag && !Icons.displayInNametag()) return "";
        if ((tablist && !Icons.displayInTab()) || profile == null || profile.name() == null || PlayerUtil.isNicked(profile)) return "";
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && profile.name().equals(mc.player.getGameProfile().name()) && Settings.bool(stats, "urchinIgnoreSelf", stats.urchinIgnoreSelf)) {
            return "";
        }
        if (profile.id() != null && profile.id().version() == 2) return "";
        StatsContainer cached = Stats.getCachedUrchin(profile.name());
        if (cached == null) {
            if (!Stats.urchinKey().isEmpty()) Stats.requestUrchin(profile.name(), ignored -> {});
            return "";
        }
        if (cached.urchinTags == null || cached.urchinTags.isEmpty()) return "";
        StringBuilder urchinTag = new StringBuilder();
        for (StatsContainer.UrchinTag tag : cached.urchinTags) {
            if (tag != null && tag.type != null) urchinTag.append(iconFor(tag));
        }
        return urchinTag.toString();
    }

    private static String iconFor(StatsContainer.UrchinTag tag) {
        String blatantTag = tag.type.equals("blatant_cheater") ? DARK_RED_SPIKE : "";
        String confirmedTag = tag.type.equals("confirmed_cheater") ? PURPLE_SPIKE : "";
        String closetTag = tag.type.equals("closet_cheater") ? YELLOW_STAR : "";
        String sniperTag = tag.type.equals("sniper") ? RED_SPIKE : "";
        String infoTag = tag.type.equals("info") ? GRAY_SPIKE : "";
        String accountTag = tag.type.equals("account") ? DARK_GRAY_SPIKE : "";
        String cautionTag = tag.type.equals("caution") ? YELLOW_INFO : "";
        return blatantTag + confirmedTag + closetTag + sniperTag + infoTag + accountTag + cautionTag;
    }
}
