package wtf.tatp.meowtils.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import wtf.tatp.meowtils.event.api.*;

class EventManagerTest {
    private static class TestEvent extends Event {}
    private static final class ChildEvent extends TestEvent {}
    private static final class Listener {
        int calls;
        @EventTarget public void receive(TestEvent event) { calls++; }
    }

    private static class BaseListener {
        int calls;
        @EventTarget private void receive(TestEvent event) { calls++; }
    }

    private static final class ChildListener extends BaseListener {}

    @Test
    void registersAndUnregistersListeners() {
        Listener listener = new Listener();
        EventManager.register(listener);
        EventManager.post(new TestEvent());
        assertEquals(1, listener.calls);
        EventManager.unregister(listener);
        EventManager.post(new TestEvent());
        assertEquals(1, listener.calls);
    }

    @Test
    void subclassPostsReachParentListeners() {
        Listener listener = new Listener();
        EventManager.register(listener);
        EventManager.post(new ChildEvent());
        EventManager.unregister(listener);
        assertEquals(1, listener.calls);
    }

    @Test
    void otherEventTypesAreNotInvoked() {
        Listener listener = new Listener();
        EventManager.register(listener);
        EventManager.post(new Event() {});
        EventManager.unregister(listener);
        assertEquals(0, listener.calls);
    }

    @Test
    void inheritedHandlersAreDiscovered() {
        ChildListener listener = new ChildListener();
        EventManager.register(listener);
        EventManager.post(new TestEvent());
        EventManager.unregister(listener);
        assertEquals(1, listener.calls);
    }
}
