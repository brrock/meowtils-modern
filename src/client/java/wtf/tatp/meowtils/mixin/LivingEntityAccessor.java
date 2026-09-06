package wtf.tatp.meowtils.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("noJumpDelay")
    int meowtils$getNoJumpDelay();

    @Accessor("noJumpDelay")
    void meowtils$setNoJumpDelay(int delay);
}
