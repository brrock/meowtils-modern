package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.module.hypixel.AccountHider;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void meowtils$customSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        if (self != Minecraft.getInstance().player) return;
        PlayerSkin replaced = AccountHider.applySkin(cir.getReturnValue());
        if (replaced != cir.getReturnValue()) cir.setReturnValue(replaced);
    }
}
