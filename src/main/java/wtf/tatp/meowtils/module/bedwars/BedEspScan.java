package wtf.tatp.meowtils.module.bedwars;

/** Hypixel/1.8 beds live in Y 0–255. Skip 26.2 void and build-height sections. */
public final class BedEspScan {
    public static final int STALE_TICKS = 20;

    private BedEspScan() {}

    public static boolean touchesLegacyWorld(int sectionBaseY) {
        return sectionBaseY <= 255 && sectionBaseY + 15 >= 0;
    }

    /** Both halves of a bed collapse to one key: the smaller packed position. */
    public static long canonicalLong(long first, long second) {
        return first <= second ? first : second;
    }

    public static boolean isStale(int consecutiveMisses) {
        return consecutiveMisses >= STALE_TICKS;
    }
}
