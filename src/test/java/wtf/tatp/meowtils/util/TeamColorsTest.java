package wtf.tatp.meowtils.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeamColorsTest {
    @Test void mostFrequentColorIgnoresFormattingAndPicksTheWinner() {
        assertNull(TeamColors.mostFrequent("Steve"));
        assertEquals("§c", TeamColors.mostFrequent("§cRed§r §cRed§aGreen"));
        assertEquals("§9", TeamColors.mostFrequent("§9[B] §9Player"));
    }
}