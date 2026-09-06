package wtf.tatp.meowtils.event;

import wtf.tatp.meowtils.event.api.Event;
import wtf.tatp.meowtils.manager.NotificationManager;

/** Posted on the caller's thread; cancel to replace the stock notification. */
public final class NotificationEvent extends Event {
    private final String title, message;
    private final NotificationManager.Type type;
    private final long duration;
    public NotificationEvent(String title, String message, NotificationManager.Type type, long duration) {
        this.title=title; this.message=message; this.type=type; this.duration=duration;
    }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationManager.Type getType() { return type; }
    public long getDuration() { return duration; }
}
