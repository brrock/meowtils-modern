package wtf.tatp.meowtils.stats;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;

/** Queues stats fetches on a daemon scheduler so the client thread never blocks. */
public final class StatsManager {
    private static final int FETCH_TIMEOUT = 10000;
    private static final ArrayDeque<FetchTask> QUEUE = new ArrayDeque<>();
    private static final HashSet<String> QUEUED_KEYS = new HashSet<>();
    private static final ConcurrentHashMap<String, List<Callback>> PENDING_FETCHES = new ConcurrentHashMap<>();
    private static boolean schedulerActive;
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Meowtils-Stats-Scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private StatsManager() {}

    public interface Callback {
        void call(StatsContainer stats);
    }

    public static void request(String name, StatsSource source, Callback callback) {
        if (name == null || source == null) {
            safeCallback(callback, null);
            return;
        }
        String lowerName = name.toLowerCase();
        String key = source.getId() + ":" + lowerName;
        StatsContainer cached = StatsCache.getValid(key);
        if (cached != null) {
            safeCallback(callback, cached);
            return;
        }
        if (!StatsCache.canRetry(key)) {
            safeCallback(callback, null);
            return;
        }
        synchronized (QUEUE) {
            if (PENDING_FETCHES.containsKey(key)) {
                if (callback != null) PENDING_FETCHES.get(key).add(callback);
                return;
            }
            if (callback != null) {
                PENDING_FETCHES.computeIfAbsent(key, ignored -> Collections.synchronizedList(new ArrayList<>())).add(callback);
            }
            if (QUEUED_KEYS.add(key)) {
                QUEUE.add(new FetchTask(lowerName, key, source));
                if (!schedulerActive) {
                    schedulerActive = true;
                    SCHEDULER.execute(StatsManager::runNext);
                }
            }
        }
    }

    private static void runNext() {
        FetchTask task;
        synchronized (QUEUE) {
            task = QUEUE.poll();
            if (task == null) {
                schedulerActive = false;
                return;
            }
        }
        runFetch(task);
        SCHEDULER.schedule(StatsManager::runNext, task.source.getCooldown(), TimeUnit.MILLISECONDS);
    }

    private static void runFetch(FetchTask task) {
        CompletableFuture<StatsContainer> future = task.source.fetch(task.name);
        ScheduledFuture<?> fallback = SCHEDULER.schedule(() -> future.cancel(true), FETCH_TIMEOUT, TimeUnit.MILLISECONDS);
        future.whenComplete((result, error) -> {
            fallback.cancel(false);
            synchronized (QUEUE) {
                QUEUED_KEYS.remove(task.key);
            }
            completedFetch(task.key, error != null ? null : result);
        });
    }

    private static void completedFetch(String key, StatsContainer stats) {
        if (stats != null) StatsCache.put(key, stats);
        else StatsCache.markFailed(key);
        notifyCallbacks(key, stats);
    }

    private static void notifyCallbacks(String key, StatsContainer stats) {
        List<Callback> callbacks;
        synchronized (QUEUE) {
            callbacks = PENDING_FETCHES.remove(key);
        }
        if (callbacks == null) return;
        for (Callback callback : callbacks) safeCallback(callback, stats);
    }

    private static void safeCallback(Callback callback, StatsContainer stats) {
        if (callback == null) return;
        Minecraft.getInstance().execute(() -> {
            try {
                callback.call(stats);
            } catch (Exception exception) {
                Meowtils.error("Failed stats callback: " + exception);
            }
        });
    }

    private static final class FetchTask {
        final String name;
        final String key;
        final StatsSource source;

        FetchTask(String name, String key, StatsSource source) {
            this.name = name;
            this.key = key;
            this.source = source;
        }
    }
}
