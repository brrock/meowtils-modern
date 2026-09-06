package wtf.tatp.meowtils.mixin;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.bedwars.BedESP;

@Mixin(LevelChunk.class)
public abstract class BedEspChunkMixin {
    @Inject(method = "replaceWithPacketData", at = @At("TAIL"))
    private void meowtils$scanBeds(FriendlyByteBuf buffer, Map<Heightmap.Types, long[]> heightmaps, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> output, CallbackInfo callback) {
        BedESP module = Module.get(BedESP.class);
        if (module != null && module.getState()) BedESP.updateChunk((LevelChunk) (Object) this);
    }
}
