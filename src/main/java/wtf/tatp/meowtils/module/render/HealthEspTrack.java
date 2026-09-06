package wtf.tatp.meowtils.module.render;

/** Holds the last sane Via/1.8 health ratio so HealthESP does not flash empty. */
public final class HealthEspTrack {
    public static final int STALE_TICKS = 20;

    private HealthEspTrack() {}

    public static boolean isStale(int consecutiveMisses) {
        return consecutiveMisses >= STALE_TICKS;
    }

    public static float displayRatio(float health, float maxHealth, float absorption, float lastRatio, boolean dying) {
        float max = Math.max(0.01f, maxHealth);
        float current = Math.max(0.0f, health + Math.max(0.0f, absorption));
        float ratio = Math.min(1.0f, current / max);
        if (dying) return ratio;
        if (current <= 0.0f && lastRatio > 0.05f) return lastRatio;
        return ratio;
    }

    public static float smooth(float previous, float target, float factor) {
        float clamped = Math.max(0.0f, Math.min(1.0f, factor));
        return previous + (target - previous) * clamped;
    }
}
