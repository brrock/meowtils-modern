package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.manager.icons.IconManager;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void meowtils$tabIcons(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || info == null) return;
        String prefix = IconManager.buildPrefix(info.getProfile(), true, false);
        String suffix = IconManager.buildSuffix(info.getProfile(), true, false);
        Component value = cir.getReturnValue();
        if ((prefix != null && !prefix.isEmpty()) || (suffix != null && !suffix.isEmpty())) {
            MutableComponent out = Component.empty();
            if (prefix != null && !prefix.isEmpty()) out.append(Component.literal(prefix));
            out.append(value);
            if (suffix != null && !suffix.isEmpty()) out.append(Component.literal(suffix));
            value = out;
        }
        Component renamed = wtf.tatp.meowtils.module.hypixel.AccountHider.renameTab(info, value);
        if (renamed != cir.getReturnValue()) cir.setReturnValue(renamed);
    }
}
