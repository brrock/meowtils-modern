package wtf.tatp.meowtils.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventManager;

/** Bridges modern styled text to the mutable extension text event. */
public final class RenderTextHooks {
    private RenderTextHooks() {}
    public static String apply(String text) {
        if (text == null || text.isEmpty() || !EventManager.hasListeners(RenderStringEvent.class)) return text;
        RenderStringEvent event = new RenderStringEvent(text);
        EventManager.post(event);
        return event.getString();
    }
    public static FormattedCharSequence apply(FormattedCharSequence sequence) {
        if (sequence == null || !EventManager.hasListeners(RenderStringEvent.class)) return sequence;
        StringBuilder encoded = new StringBuilder();
        Style[] previous = {null};
        sequence.accept((index, style, codepoint) -> {
            if (!style.equals(previous[0])) {
                encoded.append("§r");
                if (style.getColor() != null) {
                    int rgb = style.getColor().getValue();
                    ChatFormatting legacy = null;
                    for (ChatFormatting format : ChatFormatting.values())
                        if (ColorUtil.isColor(format) && (ColorUtil.rgbFromFormatting(format) & 0xFFFFFF) == rgb) { legacy = format; break; }
                    if (legacy != null) encoded.append(legacy);
                    else {
                        encoded.append("§x");
                        for (char c : String.format(java.util.Locale.ROOT, "%06x", rgb).toCharArray()) encoded.append('§').append(c);
                    }
                }
                if (style.isBold()) encoded.append("§l");
                if (style.isItalic()) encoded.append("§o");
                if (style.isUnderlined()) encoded.append("§n");
                if (style.isStrikethrough()) encoded.append("§m");
                if (style.isObfuscated()) encoded.append("§k");
                previous[0] = style;
            }
            encoded.appendCodePoint(codepoint);
            return true;
        });
        String original = encoded.toString();
        String replaced = apply(original);
        if (original.equals(replaced)) return sequence;
        if (replaced == null) return FormattedCharSequence.EMPTY;
        List<FormattedCharSequence> runs = new ArrayList<>();
        Style style = Style.EMPTY;
        StringBuilder run = new StringBuilder();
        for (int i=0; i<replaced.length();) {
            int codepoint = replaced.codePointAt(i); i += Character.charCount(codepoint);
            if (codepoint == '§' && i<replaced.length()) {
                if (!run.isEmpty()) { runs.add(FormattedCharSequence.forward(run.toString(), style)); run.setLength(0); }
                char code = Character.toLowerCase(replaced.charAt(i++));
                if (code == 'x' && i+12<=replaced.length()) {
                    StringBuilder hex = new StringBuilder();
                    for (int j=0;j<6;j++) if (replaced.charAt(i+j*2)=='§') hex.append(replaced.charAt(i+j*2+1));
                    try { if(hex.length()==6) { style=Style.EMPTY.withColor(Integer.parseInt(hex.toString(),16));i+=12;continue; } } catch (NumberFormatException ignored) { }
                }
                ChatFormatting format = ChatFormatting.getByCode(code);
                if (format != null) style = format == ChatFormatting.RESET ? Style.EMPTY : style.applyLegacyFormat(format);
            } else run.appendCodePoint(codepoint);
        }
        if (!run.isEmpty()) runs.add(FormattedCharSequence.forward(run.toString(), style));
        return FormattedCharSequence.composite(runs);
    }
}
