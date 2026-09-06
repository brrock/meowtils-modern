package wtf.tatp.meowtils.manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageManagerTest {
    @Test
    void queuesNonBlankMessages() {
        MessageManager.add("sdk-message");
        MessageManager.add("   ");
        assertTrue(MessageManager.visible(0).contains("sdk-message"));
    }
}
