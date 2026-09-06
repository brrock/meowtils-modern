package wtf.tatp.meowtils.module.render;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.handler.LatencyHandler;
import wtf.tatp.meowtils.util.Settings;

/** Status-ping HUD matching the original LatencyHandler source. */
public final class PingHUD extends Module {
    private static final long PING_FREQUENCY = 60_000L;
    @wtf.tatp.meowtils.config.Config public int posX, posY;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    private static long lastPingTime;
    private static volatile int ping;

    public PingHUD() {
        super("PingHUD", Category.Render);
        ToggleValue dynamic = new ToggleValue("Dynamic color", "dynamicColor", this);
        ToggleValue label = new ToggleValue("Text", "text", this);
        addToggle(dynamic);
        addToggle(label);
        addToggle(new ToggleValue("Brackets", "brackets", this));
        dynamic.set(true);
        label.set(true);
        tooltip("Displays your ping.");
        tag(ModuleTag.LEGIT);
    }

    @Override
    public java.util.List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        return java.util.List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("[Ping: 1000ms]", 1, scale)));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        long now = System.currentTimeMillis();
        if (now - lastPingTime < PING_FREQUENCY) return;
        lastPingTime = now;
        ServerData server = mc.getCurrentServer();
        if (server == null) return;
        LatencyHandler.ping(server, ms -> ping = ms);
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!wtf.tatp.meowtils.gui.GuiUtil.inEditor() && mc.gui.screen() != null) return;
        int value = mc.isLocalServer() ? 0 : ping;
        String label = Settings.bool(this, "text", true) ? "Ping: " : "";
        boolean brackets = Settings.bool(this, "brackets", false);
        String text = (brackets ? "[" : "") + label + latencyColor(value) + value + "ms"
                + (brackets ? ChatFormatting.WHITE + "]" : "");
        wtf.tatp.meowtils.font.HudFont.draw(event.getGraphics(), text, posX, posY, (float) Settings.number(this, "scale", scale), 0xFFFFFFFF);
    }

    private ChatFormatting latencyColor(int ms) {
        return Settings.bool(this, "dynamicColor", true) ? LatencyHandler.getLatencyColor(ms) : ChatFormatting.WHITE;
    }

    @Override
    public void onReset() {
        lastPingTime = 0L;
    }

    @Override
    public void onDisable() {
        lastPingTime = 0L;
    }
}
