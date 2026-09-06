package wtf.tatp.meowtils.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.bedwars.BedESP;

@Mixin(ClientLevel.class)
public abstract class BedEspLevelMixin {
    @Inject(method = "setBlock", at = @At("TAIL"))
    private void meowtils$updateBed(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> callback) {
        BedESP module = Module.get(BedESP.class);
        if (module != null && module.getState()) BedESP.updateBlocks(pos, state);
    }
}
