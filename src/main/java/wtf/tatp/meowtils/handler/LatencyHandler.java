package wtf.tatp.meowtils.handler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.server.network.EventLoopGroupHolder;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;

/** Always-on inbound packet clock plus the original status-ping helpers. */
public final class LatencyHandler {
    private static volatile long lastPacket = System.currentTimeMillis();

    public interface PingCallback {
        void done(int ms);
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        lastPacket = System.currentTimeMillis();
    }

    public static void markPacketReceived() {
        lastPacket = System.currentTimeMillis();
    }

    public static long getLastPacket() {
        return lastPacket;
    }

    public static ChatFormatting getLatencyColor(int ms) {
        if (ms < 40) return ChatFormatting.DARK_GREEN;
        if (ms < 100) return ChatFormatting.GREEN;
        if (ms < 150) return ChatFormatting.YELLOW;
        if (ms < 200) return ChatFormatting.GOLD;
        if (ms < 300) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

    public static void ping(ServerData server, PingCallback callback) {
        new Thread(() -> {
            ServerStatusPinger pinger = new ServerStatusPinger();
            try {
                ServerData probe = new ServerData(server.name, server.ip, server.type());
                boolean[] done = {false};
                pinger.pingServer(probe, () -> finish(done, callback, (int) probe.ping),
                        () -> finish(done, callback, -1), EventLoopGroupHolder.remote(false));
                long deadline = System.currentTimeMillis() + 8000L;
                while (!done[0] && System.currentTimeMillis() < deadline) {
                    pinger.tick();
                    Thread.sleep(50L);
                }
                finish(done, callback, -1);
            } catch (Exception e) {
                callback.done(-1);
                Meowtils.error("Unable to ping server: " + e);
            } finally {
                pinger.removeAll();
            }
        }, "Meowtils-Ping").start();
    }

    private static void finish(boolean[] done, PingCallback callback, int ms) {
        if (done[0]) return;
        done[0] = true;
        callback.done(ms);
    }
}
