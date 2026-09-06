package wtf.tatp.meowtils.gui;

import java.util.ArrayList;
import java.util.List;
import wtf.tatp.meowtils.gui.values.*;

/** Input math shared by the screen and headless interaction tests. */
public final class GuiInteraction {
    private GuiInteraction() {}
    public static float scale(String mode, int framebufferWidth, double minecraftScale) {
        float custom = switch (mode) {
            case "Tiny" -> .8f; case "Small" -> .9f; case "Large" -> 1.1f; case "Huge" -> 1.2f;
            case "Auto" -> framebufferWidth / 1920f; default -> 1f;
        };
        return (float) (3 * custom / Math.max(1, minecraftScale));
    }
    public static List<Value<?>> visibleValues(List<Object> values) {
        List<Value<?>> result = new ArrayList<>();
        for (Object raw : values) if (raw instanceof Value<?> value) {
            result.add(value);
            if (value instanceof ExpandValue expand && expand.getState()) result.addAll(visibleValues(expand.getSubValues()));
        }
        return result;
    }
    public static boolean isTrack(Value<?> value) {
        return value instanceof SliderValue || value instanceof ColorValue || value instanceof SaturationValue || value instanceof BrightnessValue;
    }
    public static void drag(Value<?> value, double mouseX, double left, double width) {
        double p=Math.max(0, Math.min(1, (mouseX-left)/width));
        if (value instanceof SliderValue slider) slider.set(slider.getMin()+p*(slider.getMax()-slider.getMin()));
        else if (value instanceof ColorValue color) {
            ColorLink link=color.getLink(); link.apply((float)p, link.getSaturation(), link.getBrightness());
        } else if (value instanceof SaturationValue saturation) saturation.setValue(p);
        else if (value instanceof BrightnessValue brightness) brightness.setValue(p);
    }
    public static String appendText(String current, String input) {
        StringBuilder result = new StringBuilder(current == null ? "" : current);
        input.codePoints().filter(c -> c >= 32 && c != 127 && c != 167).forEach(c -> {
            if (result.length()+Character.charCount(c)<=256) result.appendCodePoint(c);
        });
        return result.toString();
    }
    public static String backspace(String text) {
        return text.isEmpty() ? text : text.substring(0, text.offsetByCodePoints(text.length(), -1));
    }
}
