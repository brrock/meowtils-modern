package wtf.tatp.meowtils.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wtf.tatp.meowtils.module.hypixel.AccountHider;
import wtf.tatp.meowtils.module.render.Animations;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @ModifyVariable(method = "submitArmWithItem", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float meowtils$cancelSwing(float swingProgress) {
        return Animations.cancelSwing() ? 0.0f : swingProgress;
    }

    @WrapOperation(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;"), require = 0)
    private ItemUseAnimation meowtils$rewriteUseAnimation(ItemStack stack, Operation<ItemUseAnimation> original) {
        return Animations.rewriteUseAnimation(original.call(stack));
    }

    @WrapOperation(method = {"renderPlayerArm", "renderMapHand"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/ClientAsset$Texture;texturePath()Lnet/minecraft/resources/Identifier;"), require = 0)
    private Identifier meowtils$customArmTexture(ClientAsset.Texture texture, Operation<Identifier> original) {
        Identifier custom = AccountHider.armTexture();
        return custom != null ? custom : original.call(texture);
    }
}
