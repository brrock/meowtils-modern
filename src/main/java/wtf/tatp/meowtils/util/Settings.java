package wtf.tatp.meowtils.util;

import wtf.tatp.meowtils.gui.Module;

/** Read restored original controls after OriginalModuleSettings.install() overwrites constructor values. */
public final class Settings {
    private Settings() {}
    public static boolean bool(Module module, String key, boolean fallback) {
        return Boolean.parseBoolean(String.valueOf(module.settingsStorage().getOrDefault(key, fallback)));
    }
    public static int integer(Module module, String key, int fallback) {
        Object value = module.settingsStorage().getOrDefault(key, fallback);
        if (value instanceof Number number) return number.intValue();
        try { return (int) Double.parseDouble(String.valueOf(value)); } catch (NumberFormatException ignored) { return fallback; }
    }
    public static double number(Module module, String key, double fallback) {
        Object value = module.settingsStorage().getOrDefault(key, fallback);
        if (value instanceof Number number) return number.doubleValue();
        try { return Double.parseDouble(String.valueOf(value)); } catch (NumberFormatException ignored) { return fallback; }
    }
    public static String text(Module module, String key, String fallback) {
        Object value = module.settingsStorage().getOrDefault(key, fallback);
        return value == null ? fallback : String.valueOf(value);
    }
}
