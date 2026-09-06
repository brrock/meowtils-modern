package wtf.tatp.meowtils.manager.updater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UpdateVersionTest {
    @Test
    void bumpStartsFromZero() {
        assertEquals("0.0.1", UpdateVersion.bump("0.0.0", "patch"));
        assertEquals("0.1.0", UpdateVersion.bump("0.0.0", "minor"));
        assertEquals("1.0.0", UpdateVersion.bump("0.0.0", "major"));
    }

    @Test
    void bumpResetsLowerSegments() {
        assertEquals("1.3.0", UpdateVersion.bump("1.2.9", "minor"));
        assertEquals("2.0.0", UpdateVersion.bump("1.9.4", "major"));
        assertEquals("1.2.10", UpdateVersion.bump("1.2.9", "patch"));
    }

    @Test
    void compareTreatsTagPrefixAndPrerelease() {
        assertTrue(UpdateVersion.compare("0.0.0", "0.0.1") < 0);
        assertTrue(UpdateVersion.compare("0.0.1", "v0.0.1") == 0);
        assertTrue(UpdateVersion.compare("1.0.0-alpha.1", "1.0.0") < 0);
        assertTrue(UpdateVersion.compare("1.0.0", "0.9.9") > 0);
    }

    @Test
    void picksReleaseJarNotSourcesOrUpdater() {
        assertTrue(UpdateVersion.isModJar("meowtils-0.0.1.jar"));
        assertFalse(UpdateVersion.isModJar("meowtils-0.0.1-sources.jar"));
        assertFalse(UpdateVersion.isModJar("MeowtilsAutoUpdate.jar"));
        assertFalse(UpdateVersion.isModJar("readme.txt"));
    }
}
