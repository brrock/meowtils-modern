package wtf.tatp.meowtils.util.anticheat.checks;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;
import wtf.tatp.meowtils.util.Settings;

public final class NoSlowCheck {
    private int noSlowTicks;
    private double lastPosX;
    private double lastPosZ;

    public void anticheatCheck(Player player) {
        AntiCheat module = Module.get(AntiCheat.class);
        if (module == null || !Settings.bool(module, "noSlow", true)) {
            lastPosX = player.getX();
            lastPosZ = player.getZ();
            return;
        }
        double deltaX = player.getX() - lastPosX;
        double deltaZ = player.getZ() - lastPosZ;
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        lastPosX = player.getX();
        lastPosZ = player.getZ();
        if (player.isSprinting() && player.isUsingItem() && !player.isPassenger()) {
            double threshold = 0.05;
            var speedEffect = player.getEffect(MobEffects.SPEED);
            if (speedEffect != null) threshold = 0.05 * (1.0 + 0.2 * (speedEffect.getAmplifier() + 1));
            if (speed > threshold) { noSlowTicks++; return; }
        }
        noSlowTicks = 0;
    }

    public boolean failedNoSlow() { return noSlowTicks > 20; }
    public void reset() { noSlowTicks = 0; }
}
