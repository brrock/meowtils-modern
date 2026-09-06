package wtf.tatp.meowtils.manager;

import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import wtf.tatp.meowtils.font.HudFont;
import wtf.tatp.meowtils.gui.GuiPainter;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.api.EventTarget;

/** The original 2.0.1 single-slot notification overlay. */
public final class NotificationManager {
    public enum Type { INFO, ALERT, WARNING }
    private static final Identifier PANEL = texture("notification");
    private static final Identifier INFO = texture("icon/info");
    private static final Identifier ALERT = texture("icon/alert");
    private static final Identifier WARNING = texture("icon/warning");
    private static volatile Notification active;
    public NotificationManager() {}

    public static void show(String title, String message, Type type, long time) { show(title, 7, message, 5, type, time); }
    public static void show(String title, float titleScale, String message, float messageScale, Type type, long time) {
        var event = new wtf.tatp.meowtils.event.NotificationEvent(title, message, Objects.requireNonNull(type), time);
        wtf.tatp.meowtils.event.api.EventManager.post(event);
        if (event.isCancelled()) return;
        active = new Notification(title, message, type, System.currentTimeMillis(), time, titleScale, messageScale);
    }
    public static boolean isDisplaying() {
        Notification notification = active;
        return notification != null && !expired(notification, System.currentTimeMillis() - notification.started);
    }
    @EventTarget
    public void onRender(RenderGameOverlayEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        Notification notification = active;
        if (notification == null) return;
        long elapsed = System.currentTimeMillis() - notification.started;
        if (expired(notification, elapsed)) {
            if (active == notification) active = null;
            return;
        }
        float xOffset;
        if (elapsed < 250) xOffset = 100 * (1 - elapsed / 250f);
        else if (elapsed < notification.duration) xOffset = 0;
        else xOffset = 100 * ((elapsed - notification.duration) / 250f);
        var graphics = event.getGraphics();
        float scale = GuiUtil.getScale();
        int x = (int) (graphics.guiWidth() / scale - 100 + xOffset);
        int y = (int) (graphics.guiHeight() / scale - 25 - 10);
        Identifier icon = switch (notification.type) { case INFO -> INFO; case ALERT -> ALERT; case WARNING -> WARNING; };
        ChatFormatting formatting = switch (notification.type) {
            case INFO -> ChatFormatting.WHITE;
            case ALERT -> ChatFormatting.GOLD;
            case WARNING -> ChatFormatting.DARK_RED;
        };
        int color = ColorUtil.rgbFromFormatting(formatting);
        GuiPainter painter = new GuiPainter(graphics, client.font);
        graphics.pose().pushMatrix();
        try {
            graphics.pose().scale(scale, scale);
            painter.texture(PANEL, x, y, 100, 25, 0xFFFFFFFF);
            painter.texture(icon, x + 5, y + 4, 9, 9, color);
            // Notifications always use the TTF, independently of the HUD font setting.
            HudFont.draw(graphics, notification.title, x + 18, y + 6, notification.titleScale / 10f, color, true);
            HudFont.draw(graphics, notification.message, x + 19, y + 16, notification.messageScale / 10f, 0xFFFFFFFF, true);
        } finally {
            graphics.pose().popMatrix();
        }
    }
    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath("meowtils", "textures/notification/" + name + ".png");
    }
    private static boolean expired(Notification notification, long elapsed) {
        // Preserve the original branch order, including durations shorter than slide-in.
        return elapsed >= 250 && elapsed >= notification.duration && elapsed - notification.duration >= 250;
    }
    private record Notification(String title, String message, Type type, long started, long duration, float titleScale, float messageScale) {}
}
