package wtf.tatp.meowtils.manager.updater;

/** Semver compare/bump used by auto-update and the release workflow. */
public final class UpdateVersion {
    private UpdateVersion() {}

    public static String bump(String current, String kind) {
        String base = stripPre(current == null || current.isBlank() ? "0.0.0" : current.trim());
        String[] parts = (base + ".0.0.0").split("\\.");
        int major = parseInt(parts[0]);
        int minor = parseInt(parts[1]);
        int patch = parseInt(parts[2]);
        if ("major".equalsIgnoreCase(kind)) return (major + 1) + ".0.0";
        if ("minor".equalsIgnoreCase(kind)) return major + "." + (minor + 1) + ".0";
        return major + "." + minor + "." + (patch + 1);
    }

    /** Positive when {@code installed} is newer than {@code latest}. */
    public static int compare(String installed, String latest) {
        String a = installed == null ? "" : installed.trim();
        String b = latest == null ? "" : latest.trim();
        if (b.startsWith("v") || b.startsWith("V")) b = b.substring(1);
        if (a.startsWith("v") || a.startsWith("V")) a = a.substring(1);
        String baseA = stripPre(a);
        String baseB = stripPre(b);
        String[] splitA = baseA.split("\\.");
        String[] splitB = baseB.split("\\.");
        int length = Math.max(splitA.length, splitB.length);
        for (int i = 0; i < length; i++) {
            int valueA = i < splitA.length ? parseInt(splitA[i]) : 0;
            int valueB = i < splitB.length ? parseInt(splitB[i]) : 0;
            if (valueA != valueB) return Integer.compare(valueA, valueB);
        }
        boolean preA = a.contains("-");
        boolean preB = b.contains("-");
        if (preA && !preB) return -1;
        if (!preA && preB) return 1;
        if (!preA) return 0;
        String[] subA = a.substring(a.indexOf('-') + 1).split("\\D+");
        String[] subB = b.substring(b.indexOf('-') + 1).split("\\D+");
        int subLength = Math.max(subA.length, subB.length);
        for (int i = 0; i < subLength; i++) {
            int valueA = i < subA.length ? parseInt(subA[i]) : 0;
            int valueB = i < subB.length ? parseInt(subB[i]) : 0;
            if (valueA != valueB) return Integer.compare(valueA, valueB);
        }
        return 0;
    }

    public static boolean isModJar(String name) {
        if (name == null || !name.endsWith(".jar")) return false;
        String lower = name.toLowerCase();
        if (lower.contains("sources") || lower.contains("javadoc") || lower.contains("autoupdate")) return false;
        return lower.startsWith("meowtils-") || lower.equals("meowtils.jar");
    }

    private static String stripPre(String value) {
        int dash = value.indexOf('-');
        return dash < 0 ? value : value.substring(0, dash);
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.replaceAll("\\D", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
