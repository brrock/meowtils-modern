package wtf.tatp.meowtils.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface DelayRemoverGameModeAccessor {
    @Accessor("destroyDelay")
    int meowtils$getDestroyDelay();

    @Accessor("destroyDelay")
    void meowtils$setDestroyDelay(int delay);
}
