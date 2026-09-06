package wtf.tatp.meowtils.gui.values;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValueCompatibilityTest {
    @Test
    void modeAliasesReadAndWriteTheSameValue() {
        ModeValue mode = new ModeValue("Mode", List.of("First", "Second"), "mode", null);
        assertEquals("First", mode.getMode());
        assertEquals("First", mode.getValue());
        mode.setValue("Second");
        assertTrue(mode.is("second"));
    }

    @Test
    void toggleAliasesReadAndWriteTheSameValue() {
        ToggleValue toggle = new ToggleValue("Toggle", "toggle", null);
        toggle.setState(true);
        assertTrue(toggle.getState());
        toggle.toggle();
        assertFalse(toggle.get());
    }
}
