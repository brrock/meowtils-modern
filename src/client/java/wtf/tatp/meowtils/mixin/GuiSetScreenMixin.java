package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.event.GuiOpenEvent;
import wtf.tatp.meowtils.event.api.EventManager;

@Mixin(Gui.class)
public abstract class GuiSetScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void meowtils$postGuiOpen(Screen screen, CallbackInfo callback) {
        EventManager.post(new GuiOpenEvent(screen));
    }
}
