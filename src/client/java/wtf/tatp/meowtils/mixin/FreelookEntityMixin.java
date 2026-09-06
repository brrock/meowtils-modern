package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.utility.Freelook;

/** Consumes mouse look into the detached Freelook camera instead of the local player. */
@Mixin(Entity.class)
public abstract class FreelookEntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void meowtils$freelookTurn(double yaw, double pitch, CallbackInfo callback) {
        if (!Freelook.isActive()) return;
        if ((Object) this != Minecraft.getInstance().player) return;
        Freelook.applyDelta(yaw, pitch);
        callback.cancel();
    }
}
