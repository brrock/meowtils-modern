package wtf.tatp.meowtils.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import wtf.tatp.meowtils.gui.GuiPainter;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.bedwars.BedwarsSupport;
import wtf.tatp.meowtils.module.meowtils.Settings;
import wtf.tatp.meowtils.util.ColorUtil;

/** HUD text using the original Meowtils TTF when Smooth font is on. */
public final class HudFont {
    private HudFont() {}

    public static boolean smooth() {
        Settings settings = Module.get(Settings.class);
        if (settings == null) return true;
        return wtf.tatp.meowtils.util.Settings.bool(settings, "smoothFont", settings.smoothFont);
    }

    public static void draw(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, int color) {
        draw(graphics, text, x, y, scale, color, smooth());
    }

    public static void draw(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, int color, boolean smoothFont) {
        if (text == null || text.isEmpty() || graphics == null) return;
        Minecraft client = Minecraft.getInstance();
        if (smoothFont) {
            // 2.0.1 drawScaledStringWithShadow: y += 7.5 * (scale*10) / 10
            new GuiPainter(graphics, client.font).text(text, x, y + 7.5f * scale, scale * 10f, color);
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(client.font, BedwarsSupport.legacy(text), 0, 0, color, true);
        graphics.pose().popMatrix();
    }

    public static float width(String text, float scale) {
        return width(text, scale, smooth());
    }

    public static float width(String text, float scale, boolean smoothFont) {
        if (text == null || text.isEmpty()) return 0;
        Minecraft client = Minecraft.getInstance();
        if (smoothFont) return GuiPainter.measure(text, scale * 10f);
        return client.font.width(ColorUtil.unformattedText(text)) * scale;
    }

    public static int lineOffset(float scale) {
        int height = Minecraft.getInstance().font.lineHeight;
        int extra = smooth() ? 2 : 3;
        return (int) (height * scale + extra);
    }
}
