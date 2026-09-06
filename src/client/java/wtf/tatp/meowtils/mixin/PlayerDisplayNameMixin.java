package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.manager.icons.IconManager;

@Mixin(Player.class)
public abstract class PlayerDisplayNameMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void meowtils$nametagIcons(CallbackInfoReturnable<Component> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        Player player = (Player) (Object) this;
        String prefix = IconManager.buildPrefix(player.getGameProfile(), false, true);
        String suffix = IconManager.buildSuffix(player.getGameProfile(), false, true);
        Component value = cir.getReturnValue();
        if ((prefix != null && !prefix.isEmpty()) || (suffix != null && !suffix.isEmpty())) {
            MutableComponent out = Component.empty();
            if (prefix != null && !prefix.isEmpty()) out.append(Component.literal(prefix));
            out.append(value);
            if (suffix != null && !suffix.isEmpty()) out.append(Component.literal(suffix));
            value = out;
        }
        if (player == mc.player) {
            Component renamed = wtf.tatp.meowtils.module.hypixel.AccountHider.renameComponent(value);
            if (renamed != value) value = renamed;
        }
        if (value != cir.getReturnValue()) cir.setReturnValue(value);
    }
}
