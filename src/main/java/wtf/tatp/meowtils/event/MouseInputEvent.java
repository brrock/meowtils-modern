package wtf.tatp.meowtils.event;

import wtf.tatp.meowtils.event.api.Event;

public final class MouseInputEvent extends Event {
    public enum Action { LEFT_CLICK, RIGHT_CLICK, MIDDLE_CLICK }
    private final Action action;
    public MouseInputEvent(Action action) { this.action = action; }
    public Action getAction() { return action; }
}
