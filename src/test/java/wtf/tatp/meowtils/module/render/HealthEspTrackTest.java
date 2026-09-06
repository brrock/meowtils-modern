package wtf.tatp.meowtils.module.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HealthEspTrackTest {
    @Test
    void keepsLastRatioWhenViaDropsHealthToZero() {
        assertEquals(0.8f, HealthEspTrack.displayRatio(0.0f, 20.0f, 0.0f, 0.8f, false));
    }

    @Test
    void usesRealZeroWhenPlayerIsDying() {
        assertEquals(0.0f, HealthEspTrack.displayRatio(0.0f, 20.0f, 0.0f, 0.8f, true));
    }

    @Test
    void includesAbsorptionAndClamps() {
        assertEquals(1.0f, HealthEspTrack.displayRatio(20.0f, 20.0f, 4.0f, 1.0f, false));
        assertEquals(0.5f, HealthEspTrack.displayRatio(10.0f, 20.0f, 0.0f, 1.0f, false));
    }

    @Test
    void smoothStepsTowardTarget() {
        assertEquals(0.7f, HealthEspTrack.smooth(1.0f, 0.4f, 0.5f), 1.0e-4f);
    }

    @Test
    void pruneAfterTwentyLoadedMisses() {
        org.junit.jupiter.api.Assertions.assertFalse(HealthEspTrack.isStale(0));
        org.junit.jupiter.api.Assertions.assertFalse(HealthEspTrack.isStale(19));
        org.junit.jupiter.api.Assertions.assertTrue(HealthEspTrack.isStale(20));
    }
}
