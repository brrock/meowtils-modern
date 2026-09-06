package wtf.tatp.meowtils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.render.ItemScale;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Unique
    private static final Map<ItemEntityRenderState, Float> MEOWTILS$SCALES = Collections.synchronizedMap(new WeakHashMap<>());

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void meowtils$rememberScale(ItemEntity entity, ItemEntityRenderState state, float partialTick, CallbackInfo callback) {
        MEOWTILS$SCALES.put(state, ItemScale.scaleFor(entity.getItem()));
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void meowtils$scaleItem(ItemEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo callback) {
        float scale = MEOWTILS$SCALES.getOrDefault(state, 1.0f);
        if (scale != 1.0f) pose.scale(scale, scale, scale);
    }
}
