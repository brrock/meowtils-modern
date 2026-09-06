package wtf.tatp.meowtils.manager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-game client message queue used by extensions and compatibility helpers. */
public final class MessageManager {
    private static final CopyOnWriteArrayList<Message> messages = new CopyOnWriteArrayList<>();
    private MessageManager() {}

    public static void add(String text) {
        if (text == null || text.isBlank()) return;
        messages.add(new Message(text, System.currentTimeMillis()));
        while (messages.size() > 50) messages.remove(0);
    }

    public static List<String> visible(long lifetimeMs) {
        long now = System.currentTimeMillis();
        messages.removeIf(message -> lifetimeMs > 0 && now - message.createdAt > lifetimeMs);
        return messages.stream().map(Message::text).toList();
    }

    private record Message(String text, long createdAt) {}
}
