package wtf.tatp.meowtils.module.advanced;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.StairBlock;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.mixin.KeyMappingAccessor;
import wtf.tatp.meowtils.util.DelayedTask;

/** Jumps after a 0.5-block stair step-up while sprinting. */
public final class AutoStairs extends Module {
    private static double lastY;

    public AutoStairs() {
        super("AutoStairs", Category.Advanced);
        tag(ModuleTag.BLATANT);
        tooltip("Automatically jumps when going up stairs, for faster movement.");
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        double currentY = mc.player.getY();
        if (mc.player.onGround() && mc.player.isSprinting() && !mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            double heightGain = currentY - lastY;
            BlockPos below = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
            if (Math.abs(heightGain - 0.5d) < 1.0e-6 && mc.level.getBlockState(below).getBlock() instanceof StairBlock) {
                setJump(true);
                Meowtils.debugMessage(ChatFormatting.YELLOW + "[AutoStairs]: " + ChatFormatting.RESET + "Jumped!");
                new DelayedTask(() -> setJump(false), 1);
            }
        }
        lastY = currentY;
    }

    private void setJump(boolean down) {
        net.minecraft.client.KeyMapping.set(((KeyMappingAccessor) (Object) mc.options.keyJump).meowtils$getKey(), down);
    }
}
