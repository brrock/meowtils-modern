package wtf.tatp.meowtils.module.bedwars;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventTimerSyncTest {
    @Test void readsSameLineAndNextLineClocks() {
        assertEquals(359, EventTimerSync.elapsedFromLines(List.of("diamond ii 0:01")));
        assertEquals(360 - 272, EventTimerSync.elapsedFromLines(List.of("next diamond ii", "4:32")));
        assertEquals(720 - 59, EventTimerSync.elapsedFromLines(List.of("emerald ii: 0:59")));
        assertEquals(1800 - 90, EventTimerSync.elapsedFromLines(List.of("bed gone", "1:30")));
        assertNull(EventTimerSync.elapsedFromLines(List.of("waiting for players")));
    }
}
