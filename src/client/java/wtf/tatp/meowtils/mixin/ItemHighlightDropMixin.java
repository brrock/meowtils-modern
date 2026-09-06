package wtf.tatp.meowtils.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.skywars.ItemHighlight;

@Mixin(KeyMapping.class)
public abstract class ItemHighlightDropMixin {
    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void meowtils$cancelHighlightDrop(InputConstants.Key key, boolean pressed, CallbackInfo callback) {
        if (!pressed) return;
        Minecraft client = Minecraft.getInstance();
        if (!key.equals(((KeyMappingAccessor) (Object) client.options.keyDrop).meowtils$getKey())) return;
        if (ItemHighlight.shouldCancelSelectedDrop()) callback.cancel();
    }
}
