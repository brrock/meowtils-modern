package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.event.SlotClickEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.module.bedwars.ShopHelper;

@Mixin(AbstractContainerScreen.class)
public abstract class ShopHelperScreenMixin {
    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void meowtils$highlightAffordable(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo callback) {
        if (slot == null || !slot.hasItem()) return;
        int color = ShopHelper.slotColor(slot.getItem());
        if (color != 0) graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void meowtils$shopClick(Slot slot, int slotId, int button, ContainerInput input, CallbackInfo callback) {
        if (slot == null) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        SlotClickEvent event = new SlotClickEvent(screen, slot, slotId, button, input);
        EventManager.post(event);
        if (event.isCancelled()) {
            callback.cancel();
            return;
        }
        if (!event.getReplaceClick() && event.getButton() == button && event.getInput() == input) return;
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode != null && client.player != null) {
            ContainerInput next = event.getReplaceClick() ? ContainerInput.CLONE : event.getInput();
            int nextButton = event.getReplaceClick() ? 2 : event.getButton();
            client.gameMode.handleContainerInput(screen.getMenu().containerId, slotId, nextButton, next, client.player);
        }
        callback.cancel();
    }
}
