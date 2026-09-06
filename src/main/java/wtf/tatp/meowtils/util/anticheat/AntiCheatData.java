package wtf.tatp.meowtils.util.anticheat;

import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.util.anticheat.checks.AutoBlockCheck;
import wtf.tatp.meowtils.util.anticheat.checks.KillauraCheck;
import wtf.tatp.meowtils.util.anticheat.checks.LegitScaffoldCheck;
import wtf.tatp.meowtils.util.anticheat.checks.NoSlowCheck;

public final class AntiCheatData {
    public final AutoBlockCheck autoBlockCheck = new AutoBlockCheck();
    public final NoSlowCheck noSlowCheck = new NoSlowCheck();
    public final LegitScaffoldCheck legitScaffoldCheck = new LegitScaffoldCheck();
    public final KillauraCheck killauraCheck = new KillauraCheck();
    private UUID lastUuid;

    public void anticheatCheck(Player player) {
        lastUuid = player.getUUID();
        autoBlockCheck.anticheatCheck(player);
        noSlowCheck.anticheatCheck(player);
        legitScaffoldCheck.anticheatCheck(player);
        killauraCheck.anticheatCheck(player);
    }

    public boolean failedAutoBlock() { return autoBlockCheck.failedAutoBlock(); }
    public boolean failedNoSlow() { return noSlowCheck.failedNoSlow(); }
    public boolean failedLegitScaffold() { return legitScaffoldCheck.failedLegitScaffold(lastUuid); }
    public boolean failedKillauraB() { return killauraCheck.failedKillauraB(); }
}
