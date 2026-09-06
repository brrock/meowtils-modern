package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.NoTitles;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void meowtils$hideTitle(Component title, CallbackInfo callback) {
        if (NoTitles.INSTANCE != null && NoTitles.INSTANCE.getState()) callback.cancel();
    }
    @Inject(method = "setSubtitle", at = @At("HEAD"), cancellable = true)
    private void meowtils$hideSubtitle(Component subtitle, CallbackInfo callback) {
        if (NoTitles.INSTANCE != null && NoTitles.INSTANCE.getState()) callback.cancel();
    }
}
