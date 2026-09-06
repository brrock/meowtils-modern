package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.advanced.AutoChest;
import wtf.tatp.meowtils.util.Settings;

/** Overlays the slot AutoChest just shift-clicked when Render clicked is enabled. */
@Mixin(AbstractContainerScreen.class)
public abstract class AutoChestSlotMixin {
    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void meowtils$autoChestClicked(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo callback) {
        AutoChest module = Module.get(AutoChest.class);
        if (module == null || !module.getState() || !Settings.bool(module, "renderClicked", true) || slot == null) return;
        if (AutoChest.CLICKED_SLOTS.contains(slot.index)) {
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x80FFFFFF);
        }
    }
}
