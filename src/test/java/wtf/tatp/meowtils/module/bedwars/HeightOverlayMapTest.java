package wtf.tatp.meowtils.module.bedwars;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeightOverlayMapTest {
    @Test
    void detectsGameStartTriggers() {
        assertTrue(HeightOverlayMap.isMapTrigger("The game starts in 1 second!"));
        assertTrue(HeightOverlayMap.isMapTrigger("§eThe game starts in 1 second!"));
        assertTrue(HeightOverlayMap.isMapTrigger("  §l§eThe game starts in 1 second!  "));
        assertFalse(HeightOverlayMap.isMapTrigger("The game starts in 2 seconds!"));
        assertFalse(HeightOverlayMap.isMapTrigger("the game starts in 1 second!"));
    }

    @Test
    void detectsRespawnTriggers() {
        assertTrue(HeightOverlayMap.isMapTrigger("You will respawn in 6 seconds!"));
        assertTrue(HeightOverlayMap.isMapTrigger("§cYou will respawn in 6 seconds!"));
        assertFalse(HeightOverlayMap.isMapTrigger("You will respawn in 5 seconds!"));
    }

    @Test
    void extractsMapNames() {
        assertEquals("Aquarium", HeightOverlayMap.parseMapName("You are currently playing on Aquarium"));
        assertEquals("sky rise", HeightOverlayMap.parseMapName("you are currently playing on sky rise"));
        assertEquals("sky rise", HeightOverlayMap.parseMapName("You are currently playing on sky rise."));
        assertEquals("Aquarium", HeightOverlayMap.parseMapName("You are currently playing on   Aquarium  ."));
        assertEquals("Aquarium", HeightOverlayMap.parseMapName("§aYou are currently playing on §bAquarium"));
        assertEquals("Lighthouse", HeightOverlayMap.parseMapName("§6You are currently playing on §e§lLighthouse§6."));
    }

    @Test
    void ignoresUnrelatedChat() {
        assertNull(HeightOverlayMap.parseMapName("Waiting for players..."));
        assertNull(HeightOverlayMap.parseMapName("You are currently spectating Aquarium"));
        assertFalse(HeightOverlayMap.isMapTrigger(""));
        assertFalse(HeightOverlayMap.isMapTrigger(null));
        assertNull(HeightOverlayMap.parseMapName(null));
    }
}
