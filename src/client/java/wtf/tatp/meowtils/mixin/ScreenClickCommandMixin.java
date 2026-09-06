package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.command.ChatClicks;

/** Keep /setflagmessagecolor and /settheme on the client when chat clicks them. */
@Mixin(Screen.class)
public abstract class ScreenClickCommandMixin {
    @Inject(method = "clickCommandAction", at = @At("HEAD"), cancellable = true)
    private static void meowtils$clientPrefixClicks(LocalPlayer player, String command, Screen screen, CallbackInfo callback) {
        if (ChatClicks.handle(command)) callback.cancel();
    }
}
