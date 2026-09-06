package wtf.tatp.meowtils.event;

import net.minecraft.network.chat.Component;
import wtf.tatp.meowtils.event.api.Event;

/** Normalized server/game message event for extensions and built-in modules. */
public final class ChatReceivedEvent extends Event {
    private final Component message;
    private final boolean overlay;
    public ChatReceivedEvent(Component message, boolean overlay) { this.message = message; this.overlay = overlay; }
    public Component getMessage() { return message; }
    public String getText() { return message.getString(); }
    public boolean isOverlay() { return overlay; }
}
