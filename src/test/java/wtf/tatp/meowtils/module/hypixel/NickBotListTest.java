package wtf.tatp.meowtils.module.hypixel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NickBotListTest {
    @Test
    void emptyObjectFromTouchIsEmptyList() {
        assertTrue(NickBotList.parse("{}").isEmpty());
        assertTrue(NickBotList.parse("").isEmpty());
        assertTrue(NickBotList.parse("[]").isEmpty());
    }

    @Test
    void originalArrayRoundTrips() {
        assertEquals(java.util.Set.of("cat", "dog"), NickBotList.parse("[\"cat\",\"dog\"]"));
    }

    @Test
    void namesObjectIsAccepted() {
        assertEquals(java.util.Set.of("fox"), NickBotList.parse("{\"names\":[\"fox\"]}"));
    }
}
