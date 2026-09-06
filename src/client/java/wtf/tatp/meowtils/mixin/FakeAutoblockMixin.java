package wtf.tatp.meowtils.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.module.render.Animations;

/**
 * Original MixinEntityPlayerSP {@code func_71052_bv} / {@code func_70632_aY}.
 * Do not mixin {@code LocalPlayer.isUsingItem} — that flag drives 26.2 movement slowdown
 * and would break Sprint. First-person block pose is {@code ItemInHandRendererMixin}.
 */
@Mixin(LivingEntity.class)
public abstract class FakeAutoblockMixin {
    @Inject(method = "getUseItemRemainingTicks", at = @At("RETURN"), cancellable = true)
    private void meowtils$fakeUseTicks(CallbackInfoReturnable<Integer> callback) {
        if (!((Object) this instanceof LocalPlayer)) return;
        callback.setReturnValue(Animations.fakeUseTicks(callback.getReturnValue()));
    }

    @Inject(method = "isBlocking", at = @At("RETURN"), cancellable = true)
    private void meowtils$fakeBlocking(CallbackInfoReturnable<Boolean> callback) {
        if (!((Object) this instanceof LocalPlayer)) return;
        callback.setReturnValue(Animations.fakeUsing(callback.getReturnValue()));
    }
}
