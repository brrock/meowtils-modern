package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.module.utility.AntiObfuscate;

@Mixin(Font.class)
public abstract class FontMixin {
    @ModifyVariable(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String meowtils$prepareText(String string) {
        return applyStringHook(string);
    }

    @ModifyVariable(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String meowtils$width(String string) {
        return applyStringHook(string);
    }

    @ModifyVariable(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private FormattedCharSequence meowtils$prepareSequence(FormattedCharSequence sequence) {
        return AntiObfuscate.stripPrepared(sequence);
    }

    @ModifyVariable(method = "prepare8xTextOutline(Lnet/minecraft/util/FormattedCharSequence;FFI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private FormattedCharSequence meowtils$prepareOutline(FormattedCharSequence sequence) {
        return AntiObfuscate.stripPrepared(sequence);
    }

    private static String applyStringHook(String string) {
        if (string == null || string.isEmpty()) return string;
        boolean obfuscated = AntiObfuscate.active() && string.indexOf('§') >= 0;
        boolean rename = wtf.tatp.meowtils.module.hypixel.AccountHider.needsRename(string);
        if (!obfuscated && !rename) return string;
        if (obfuscated) string = AntiObfuscate.strip(string);
        if (!rename) return string;
        RenderStringEvent event = new RenderStringEvent(string);
        EventManager.post(event);
        return event.getString();
    }
}
