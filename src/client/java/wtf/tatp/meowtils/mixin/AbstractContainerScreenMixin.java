package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.render.ShinyPots;
import wtf.tatp.meowtils.module.skywars.ItemHighlight;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void meowtils$shinyPots(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo callback) {
        int color = ShinyPots.slotColor(slot);
        if (color != 0) graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, color);
        int highlight = ItemHighlight.slotColor(slot);
        if (highlight != 0) graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, highlight);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void meowtils$cancelHighlightThrow(Slot slot, int slotId, int button, ContainerInput input, CallbackInfo callback) {
        if (ItemHighlight.shouldCancelThrow(slot, input)) callback.cancel();
    }
}
