package wtf.tatp.meowtils.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.render.AntiInvis;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void meowtils$antiInvis(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo callback) {
        if (entity instanceof Player player && AntiInvis.shouldReveal(player)) {
            AntiInvis.applyReveal(state);
        }
    }

    /**
     * Java 25 emits {@code @Redirect.at} as a one-element array; MixinExtras 0.5.4 then
     * ClassCasts that list to {@code AnnotationNode}. {@link WrapOperation} avoids that path.
     */
    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;multiply(II)I"),
            require = 0)
    private int meowtils$antiInvisTint(int color, int modelTint, Operation<Integer> original, LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (AntiInvis.revealed(state)) return AntiInvis.tint(state, color, modelTint);
        return original.call(color, modelTint);
    }
}
