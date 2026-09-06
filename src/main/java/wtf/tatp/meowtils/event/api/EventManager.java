package wtf.tatp.meowtils.event.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventManager {
    private static final EventPriority[] PRIORITIES = EventPriority.values();
    private static final EnumMap<EventPriority, List<Listener>> listeners = new EnumMap<>(EventPriority.class);
    private static final ConcurrentHashMap<Class<?>, Class<?>[]> HIERARCHY = new ConcurrentHashMap<>();
    private static volatile Map<Class<?>, Listener[]>[] index = emptyIndex();

    static { for (EventPriority priority : PRIORITIES) listeners.put(priority, new ArrayList<>()); }
    private EventManager() {}

    public static synchronized void register(Object owner) {
        unregister(owner);
        for (Class<?> type = owner.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                EventTarget target = method.getAnnotation(EventTarget.class);
                if (target == null || method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) continue;
                method.setAccessible(true);
                listeners.get(target.priority()).add(new Listener(owner, method, method.getParameterTypes()[0]));
            }
        }
        rebuild();
    }

    public static synchronized void unregister(Object owner) {
        boolean removed = false;
        for (List<Listener> list : listeners.values()) {
            removed |= list.removeIf(listener -> listener.owner == owner);
        }
        if (removed) rebuild();
    }

    public static void post(Event event) {
        Map<Class<?>, Listener[]>[] snap = index;
        if (snap == null) return;
        Class<?>[] types = typesOf(event.getClass());
        for (Map<Class<?>, Listener[]> bucket : snap) {
            for (Class<?> type : types) {
                Listener[] typed = bucket.get(type);
                if (typed == null) continue;
                for (Listener listener : typed) {
                    try { listener.method.invoke(listener.owner, event); }
                    catch (ReflectiveOperationException exception) { throw new RuntimeException("Meowtils event listener failed", exception); }
                    if (event.isCancelled()) return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<?>, Listener[]>[] emptyIndex() {
        Map<Class<?>, Listener[]>[] empty = new Map[PRIORITIES.length];
        for (int i = 0; i < empty.length; i++) empty[i] = Map.of();
        return empty;
    }

    @SuppressWarnings("unchecked")
    private static void rebuild() {
        Map<Class<?>, Listener[]>[] next = new Map[PRIORITIES.length];
        for (int i = 0; i < PRIORITIES.length; i++) {
            Map<Class<?>, List<Listener>> byType = new HashMap<>();
            for (Listener listener : listeners.get(PRIORITIES[i])) {
                byType.computeIfAbsent(listener.type, ignored -> new ArrayList<>()).add(listener);
            }
            Map<Class<?>, Listener[]> typed = new HashMap<>();
            byType.forEach((type, list) -> typed.put(type, list.toArray(Listener[]::new)));
            next[i] = Map.copyOf(typed);
        }
        index = next;
    }

    private static Class<?>[] typesOf(Class<?> posted) {
        return HIERARCHY.computeIfAbsent(posted, type -> {
            List<Class<?>> types = new ArrayList<>(4);
            for (Class<?> current = type; current != null && Event.class.isAssignableFrom(current); current = current.getSuperclass()) {
                types.add(current);
            }
            return types.toArray(Class[]::new);
        });
    }

    private record Listener(Object owner, Method method, Class<?> type) {}
}
