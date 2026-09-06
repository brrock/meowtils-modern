package wtf.tatp.meowtils.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.module.bedwars.BedwarsSupport;
import wtf.tatp.meowtils.module.bedwars.HeightOverlay;

@Mixin(Level.class)
public abstract class HeightOverlayWoolMixin {
    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    private void meowtils$recolorWool(BlockPos pos, CallbackInfoReturnable<BlockState> callback) {
        if (!((Object) this instanceof ClientLevel) || !HeightOverlay.woolOverlayEnabled()) return;
        BlockState state = callback.getReturnValue();
        if (state == null || !BedwarsSupport.isWool(state) || pos.getY() < HeightOverlay.getHeight() - 1) return;
        Block replacement = Blocks.WOOL.pick(HeightOverlay.getWoolColor());
        if (replacement == null || replacement == state.getBlock()) return;
        callback.setReturnValue(replacement.defaultBlockState());
    }
}
