package wtf.tatp.meowtils.event;
import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.event.api.*;
import wtf.tatp.meowtils.manager.NotificationManager;
import static org.junit.jupiter.api.Assertions.*;
class NotificationEventTest {
    static final class Replacement {
        NotificationEvent captured;
        @EventTarget public void receive(NotificationEvent event){captured=event;event.setCancelled(true);}
    }
    @Test void replacementReceivesNotificationAndSuppressesStockDisplay() {
        Replacement listener=new Replacement();EventManager.register(listener);
        try {
            NotificationManager.show("Test", "Message", NotificationManager.Type.WARNING,1500);
            assertNotNull(listener.captured);
            assertEquals("Test",listener.captured.getTitle());
            assertEquals(1500,listener.captured.getDuration());
            assertEquals(NotificationManager.Type.WARNING,listener.captured.getType());
            assertFalse(NotificationManager.isDisplaying());
        } finally {EventManager.unregister(listener);}
    }
}
