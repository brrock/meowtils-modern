package wtf.tatp.meowtils.mixin;

import net.minecraft.client.ClientClockManager;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.module.render.TimeChanger;

/**
 * 26.2 removed {@code Level.getDayTime()}. Sky, lighting, clock items and
 * timelines all read {@link ClientClockManager#getTotalTicks(Holder)}.
 */
@Mixin(ClientClockManager.class)
public abstract class LevelTimeMixin {
    @Inject(method = "getTotalTicks", at = @At("RETURN"), cancellable = true)
    private void meowtils$clientTime(Holder<WorldClock> clock, CallbackInfoReturnable<Long> cir) {
        long value = TimeChanger.time();
        if (value != Long.MIN_VALUE) {
            Long base = cir.getReturnValue();
            cir.setReturnValue(TimeChanger.applyClientDayTime(base == null ? 0L : base, value));
        }
    }
}
