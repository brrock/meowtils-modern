package example;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.font.HudFont;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;

/**
 * 26.2 example: settings, chat, HUD, and a Fabric client command.
 * Package as {@code hello-extension.meowtils} and drop it in {@code meowtils/extensions/}.
 */
public final class HelloExtension extends Extension {
    private static HelloExtension instance;
    private int ticks;

    private HelloExtension() {
        super("Hello Extension", "Meowtils");
        toggle("Greet on enable", "greet");
        toggle("Show HUD", "hud");
        slider("Repeat seconds", 0, 30, 1, "s", "repeat", Integer.class);
        text("Trigger word", "Word in chat that prints a local reply", "trigger");
        info("Example 26.2 extension. /hello in chat.");
        settingsStorage().putIfAbsent("greet", true);
        settingsStorage().putIfAbsent("hud", true);
        settingsStorage().putIfAbsent("repeat", 0);
        settingsStorage().putIfAbsent("trigger", "meow");
    }

    public static void init() {
        instance = new HelloExtension();
        Extension.registerModule(instance);
        Extension.registerCommand(LiteralArgumentBuilder.<FabricClientCommandSource>literal("hello")
                .executes(context -> {
                    Meowtils.addMessage("Hello from a 26.2 .meowtils extension.");
                    return 1;
                }));
    }

    @Override
    public void onEnable() {
        ticks = 0;
        if (Settings.bool(this, "greet", true)) {
            Meowtils.addMessage("Hello Extension enabled. Try /hello");
        }
    }

    @Override
    public void onDisable() {
        Meowtils.addMessage("Hello Extension disabled.");
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        int seconds = Settings.integer(this, "repeat", 0);
        if (seconds <= 0) return;
        if (++ticks < seconds * 20) return;
        ticks = 0;
        Meowtils.addMessage("Hello again (" + seconds + "s).");
    }

    @EventTarget
    public void onChat(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        String trigger = Settings.text(this, "trigger", "meow").trim();
        if (trigger.isEmpty()) return;
        String plain = ColorUtil.unformattedText(event.getText());
        if (plain.toLowerCase().contains(trigger.toLowerCase()) && !plain.contains("Hello Extension")) {
            Meowtils.addMessage("Heard \"" + trigger + "\" in chat.");
        }
    }

    @EventTarget
    public void onHud(RenderGameOverlayEvent event) {
        if (!Settings.bool(this, "hud", true) || mc.level == null || mc.gui.screen() != null) return;
        HudFont.draw(event.getGraphics(), "Hello Extension " + Meowtils.version(), 8, 8, 0.8f, 0xFFFFFFFF);
    }
}
