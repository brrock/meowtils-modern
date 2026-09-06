package wtf.tatp.meowtils.event;

import wtf.tatp.meowtils.event.api.Event;

/** Mutable text hook used by text-filtering extensions. */
public final class RenderStringEvent extends Event {
    private String string;
    public RenderStringEvent(String string) { this.string = string; }
    public String getString() { return string; }
    public void setString(String string) { this.string = string; }
}
