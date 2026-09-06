package wtf.tatp.meowtils.module.utility;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.world.entity.player.Player;
import wtf.tatp.meowtils.event.AttackEntityEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

/** Plays combat feedback for blocked incoming damage and critical hits. */
public final class ActionSounds extends Module {
    private static long hurtTime;
    private static long lastCritTime;
    private final SliderValue volume = new SliderValue("Volume", 0, 100, 5, "%", "volume", this, Integer.class);

    public ActionSounds() {
        super("ActionSounds", Category.Utility);
        addSlider(volume);
        ToggleValue blocked = new ToggleValue("Blocked damage", "blockSound", this);
        ToggleValue critical = new ToggleValue("Critical hit", "critSound", this);
        addToggle(blocked);
        addToggle(critical);
        volume.set(100);
        blocked.set(true);
        critical.set(true);
        tooltip("Plays a sound when performing certain actions.");
        tag(ModuleTag.LEGIT);
    }

    @EventTarget
    public void onPacketReceived(ReceivePacketEvent event) {
        if (mc.player == null || mc.level == null || !Settings.bool(this, "blockSound", true)) return;
        if (!localHurt(event.getPacket()) || !blocking()) return;
        long now = System.currentTimeMillis();
        if (now - hurtTime < 250) return;
        Util.playSound(Util.Sound.ANVIL, Settings.integer(this, "volume", 100));
        hurtTime = now;
    }

    @EventTarget
    public void onAttack(AttackEntityEvent event) {
        if (!Settings.bool(this, "critSound", true) || mc.player == null) return;
        if (!(event.getTarget() instanceof Player)) return;
        if (mc.player.onGround() || mc.player.fallDistance <= 0.0f) return;
        if (mc.player.isInWater() || mc.player.isInLava() || mc.player.isPassenger()) return;
        long now = System.currentTimeMillis();
        if (now - lastCritTime <= 250) return;
        Util.playSound(Util.Sound.CRIT, Settings.integer(this, "volume", 100));
        lastCritTime = now;
    }

    private boolean localHurt(Packet<?> packet) {
        int id = mc.player.getId();
        if (packet instanceof ClientboundHurtAnimationPacket hurt) return hurt.id() == id;
        if (packet instanceof ClientboundDamageEventPacket damage) return damage.entityId() == id;
        if (packet instanceof ClientboundEntityEventPacket status) {
            return status.getEventId() == 2 && status.getEntity(mc.level) == mc.player;
        }
        return false;
    }

    private boolean blocking() {
        if (mc.player.isBlocking()) return true;
        return mc.player.isUsingItem() && ItemIds.is(mc.player.getUseItem(), "sword", "shield");
    }
}
