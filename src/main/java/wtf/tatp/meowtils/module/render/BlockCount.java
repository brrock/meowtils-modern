package wtf.tatp.meowtils.module.render;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.module.advanced.AutoSwap;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

/** Held-stack block counter with the original low-count alert and ping. */
public final class BlockCount extends Module {
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 1, red = 255, green = 255, blue = 255;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    @wtf.tatp.meowtils.config.Config public boolean alert = true, sound = true;
    private static boolean alerted;

    public BlockCount() {
        super("BlockCount", Category.Render);
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addSlider(new SliderValue("Alert threshold", 1, 16, 1, "blocks", "threshold", this, Integer.TYPE));
        addToggle(new ToggleValue("Alert when low", "alert", this));
        addToggle(new ToggleValue("Ping sound", "sound", this));
        settingsStorage().put("threshold", 4);
        settingsStorage().put("scale", 0.65);
        settingsStorage().put("alert", true);
        settingsStorage().put("sound", true);
        tooltip("Displays block count on screen.");
        tag(ModuleTag.LEGIT);
    }

    @Override
    public java.util.List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        return java.util.List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("64", 1, (float) Settings.number(this, "scale", scale))));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean preview = wtf.tatp.meowtils.gui.GuiUtil.inEditor();
        if (!preview && mc.gui.screen() != null) return;
        int totalCount = 0;
        ItemStack held = mc.player.getMainHandItem();
        if (preview) {
            totalCount = 64;
        } else if (held.getItem() instanceof BlockItem) {
            AutoSwap swap = get(AutoSwap.class);
            if (swap != null && swap.getState()) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.player.getInventory().getItem(i);
                    if (AutoSwap.isValidBlock(stack)) totalCount += stack.getCount();
                }
                if (totalCount == 0) return;
            } else {
                totalCount = held.getCount();
            }
        } else {
            maybeReset(totalCount);
            return;
        }
        int threshold = Settings.integer(this, "threshold", 4);
        float drawScale = (float) Settings.number(this, "scale", scale);
        int color = totalCount <= threshold
                ? ColorUtil.rgbFromFormatting(ChatFormatting.DARK_RED)
                : ColorUtil.rgb(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue));
        wtf.tatp.meowtils.font.HudFont.draw(event.getGraphics(), Integer.toString(preview ? 64 : totalCount), posX, posY, drawScale, color);
        if (preview) return;
        if (totalCount == threshold && !alerted) {
            String end = threshold == 1 ? " block left!" : " blocks left!";
            if (Settings.bool(this, "alert", true)) {
                if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                    Meowtils.addMessage(ChatFormatting.RED + "You only have " + totalCount + end);
                }
                if (Notifications.getMode() != Notifications.Mode.CHAT) {
                    NotificationManager.show("BlockCount", ChatFormatting.RED + String.valueOf(totalCount) + end, NotificationManager.Type.ALERT, 1500L);
                }
            }
            if (Settings.bool(this, "sound", true)) Util.playSound(Util.Sound.PING_DEEP, 100);
            alerted = true;
            return;
        }
        maybeReset(totalCount);
    }

    private void maybeReset(int totalCount) {
        if (totalCount > Settings.integer(this, "threshold", 4)) alerted = false;
    }
}
