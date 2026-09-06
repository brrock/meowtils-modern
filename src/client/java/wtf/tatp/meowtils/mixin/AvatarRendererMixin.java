package wtf.tatp.meowtils.mixin;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.manager.CapeManager;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void meowtils$applyCape(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo callback) {
        ClientAsset.Texture cape = CapeManager.textureFor(entity);
        if (cape == null || state.skin == null) return;
        state.skin = new PlayerSkin(state.skin.body(), cape, state.skin.elytra(), state.skin.model(), state.skin.secure());
        state.showCape = true;
    }
}
