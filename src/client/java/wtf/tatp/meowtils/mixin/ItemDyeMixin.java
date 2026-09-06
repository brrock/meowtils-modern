package wtf.tatp.meowtils.mixin;

import net.minecraft.client.color.item.Dye;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.module.skywars.NoArmorDye;

@Mixin(Dye.class)
public abstract class ItemDyeMixin {
    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true)
    private void meowtils$undyeItem(ItemStack stack, ClientLevel level, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (NoArmorDye.shouldStripItem()) cir.setReturnValue(DyedItemColor.LEATHER_COLOR);
    }
}
