package wtf.tatp.meowtils.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wtf.tatp.meowtils.module.skywars.NoArmorDye;

@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerDyeMixin {
    /**
     * The 9-arg {@code renderLayers} overload only forwards; dye is read in the 11-arg one.
     * {@code require = 0} so a missed invoke cannot abort resource reload (black screen).
     */
    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/DyedItemColor;getOrDefault(Lnet/minecraft/world/item/ItemStack;I)I"),
            require = 0)
    private int meowtils$undyeModel(ItemStack stack, int fallback, Operation<Integer> original) {
        if (NoArmorDye.shouldStripModel()) return DyedItemColor.LEATHER_COLOR;
        return original.call(stack, fallback);
    }
}
