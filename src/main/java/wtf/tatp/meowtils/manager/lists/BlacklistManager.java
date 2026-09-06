package wtf.tatp.meowtils.manager.lists;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;

public final class BlacklistManager {
    public static final Set<String> NON_BLATANT = Set.of("cheating", "aimassist", "autoclicker", "reach", "velocity", "esp", "fakelag", "legit scaffold", "fastplace", "closet", "scaffold");
    public static final Set<String> BLATANT = Set.of("antivoid", "bednuker", "fly", "keepsprint", "killaura", "nofall", "noslow", "bhop", "antifireball", "safewalk", "blink", "autoblock", "blatant", "strafe", "snipe", "sniper", "sniping");
    private static final Path FILE = PlayerListStore.resolve(MeowtilsData.blacklist(), MeowtilsData.legacyBlacklist());
    private static Map<String, String> blacklist = PlayerListStore.loadMap(FILE);
    private BlacklistManager() {}

    public static void add(String uuidOrName, String reason) {
        blacklist.put(uuidOrName, System.currentTimeMillis() + " " + reason);
        PlayerListStore.save(FILE, blacklist);
    }
    public static void remove(String key) { blacklist.remove(key); PlayerListStore.save(FILE, blacklist); }
    public static void appendReason(String key, String reason) {
        String existing = getEntry(key);
        if (existing == null) { add(key, reason); return; }
        String[] parts = existing.split(" ", 2);
        String prev = parts.length > 1 ? parts[1] : "";
        LinkedHashSet<String> reasons = new LinkedHashSet<>();
        if (!prev.isBlank()) reasons.addAll(Arrays.asList(prev.split(" \\| ")));
        reasons.add(reason);
        remove(key);
        add(key, String.join(" | ", reasons));
    }
    public static String formatReasons(String[] reasonParts) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < reasonParts.length; i++) {
            String current = reasonParts[i].toLowerCase(Locale.ROOT);
            if (i < reasonParts.length - 1) {
                String combined = current + " " + reasonParts[i + 1].toLowerCase(Locale.ROOT);
                if (NON_BLATANT.contains(combined) || BLATANT.contains(combined)) {
                    result.add(combined);
                    i++;
                    continue;
                }
            }
            result.add(current);
        }
        return String.join(" | ", result);
    }
    public static boolean isBlacklisted(String uuidOrName) { return uuidOrName != null && blacklist.containsKey(uuidOrName); }
    public static String getEntry(String uuidOrName) { return blacklist.get(uuidOrName); }
    public static String getFormattedEntry(String uuidOrName) {
        String raw = blacklist.get(uuidOrName);
        if (raw == null) return null;
        String[] parts = raw.split(" ", 2);
        if (parts.length < 2) return null;
        try {
            long timeMs = Long.parseLong(parts[0]);
            return ChatFormatting.DARK_GRAY + Meowtils.formatTimestamp(timeMs) + ChatFormatting.GRAY + " for: " + colorReasons(parts[1]);
        } catch (NumberFormatException ignored) {
            return colorReasons(parts[1]);
        }
    }
    public static ChatFormatting getReasonColor(String entryOrReasons) {
        if (entryOrReasons == null) return ChatFormatting.DARK_GREEN;
        String[] split = entryOrReasons.split(" ", 2);
        String reasons = split.length == 2 ? split[1] : entryOrReasons;
        for (String part : reasons.toLowerCase(Locale.ROOT).split("\\|")) {
            String reason = part.trim();
            if (BLATANT.contains(reason)) return ChatFormatting.DARK_RED;
            if (NON_BLATANT.contains(reason)) return ChatFormatting.GOLD;
        }
        return ChatFormatting.DARK_GREEN;
    }
    public static String colorReasons(String raw) {
        String[] parts = raw.split(" \\| ");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String reason = parts[i].trim();
            out.append(getReasonColor(reason)).append(reason.toLowerCase(Locale.ROOT));
            if (i < parts.length - 1) out.append(ChatFormatting.DARK_GRAY).append(" | ");
        }
        return out.toString();
    }
    public static Map<String, String> all() { return Map.copyOf(blacklist); }
}
