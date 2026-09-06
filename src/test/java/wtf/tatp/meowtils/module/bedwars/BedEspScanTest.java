package wtf.tatp.meowtils.module.bedwars;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BedEspScanTest {
    @Test
    void skipsVoidAndModernBuildHeight() {
        assertFalse(BedEspScan.touchesLegacyWorld(-64));
        assertFalse(BedEspScan.touchesLegacyWorld(256));
        assertFalse(BedEspScan.touchesLegacyWorld(320));
        assertTrue(BedEspScan.touchesLegacyWorld(0));
        assertTrue(BedEspScan.touchesLegacyWorld(240));
    }

    @Test
    void bothHalvesShareOneCanonicalKey() {
        assertEquals(1L, BedEspScan.canonicalLong(5L, 1L));
        assertEquals(1L, BedEspScan.canonicalLong(1L, 5L));
        assertEquals(3L, BedEspScan.canonicalLong(3L, 3L));
    }

    @Test
    void pruneAfterTwentyLoadedMisses() {
        assertFalse(BedEspScan.isStale(0));
        assertFalse(BedEspScan.isStale(19));
        assertTrue(BedEspScan.isStale(20));
        assertTrue(BedEspScan.isStale(40));
    }
}
