package wtf.tatp.meowtils.module.utility;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;

/** Strips obfuscation from prepared text the way MixinFontRenderer removed §k. */
public final class AntiObfuscate extends Module {
    public AntiObfuscate() {
        super("AntiObfuscate", Category.Utility);
        tooltip("Deobfuscates text.");
        tag(ModuleTag.LEGIT);
    }

    public static boolean active() {
        AntiObfuscate module = get(AntiObfuscate.class);
        return module != null && module.getState();
    }

    public static String strip(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.replace("§k", "").replace("§K", "");
    }

    public static FormattedCharSequence stripPrepared(FormattedCharSequence sequence) {
        if (sequence == null || !active()) return sequence;
        return sink -> sequence.accept((index, style, codepoint) ->
                sink.accept(index, style.isObfuscated() ? style.withObfuscated(Boolean.FALSE) : style, codepoint));
    }

    @EventTarget(priority = EventPriority.LOWEST)
    public void onRenderString(RenderStringEvent event) {
        if (mc.player == null || mc.level == null || event.getString() == null) return;
        event.setString(strip(event.getString()));
    }
}
