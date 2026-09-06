package wtf.tatp.meowtils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.render.ChestESP;

@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void meowtils$chestOutline(ChestRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo callback) {
        int color = ChestESP.tesrColor(state.blockPos);
        if (color == 0) return;
        collector.order(0).submitShapeOutline(pose, Shapes.block(), RenderTypes.LINES, color, 2.0f, true);
    }
}
