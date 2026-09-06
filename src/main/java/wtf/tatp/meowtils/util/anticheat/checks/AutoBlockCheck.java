package wtf.tatp.meowtils.util.anticheat.checks;

import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

public final class AutoBlockCheck {
    private int autoBlockTicks;

    public void anticheatCheck(Player player) {
        AntiCheat module = Module.get(AntiCheat.class);
        if (module == null || !Settings.bool(module, "autoBlock", true)) return;
        boolean blocking = player.isBlocking() || (player.isUsingItem() && ItemIds.is(player.getMainHandItem(), "sword"));
        if (player.swinging && blocking) autoBlockTicks++;
        else autoBlockTicks = 0;
    }

    public boolean failedAutoBlock() { return autoBlockTicks > 10; }
    public void reset() { autoBlockTicks = 0; }
}
