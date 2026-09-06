package wtf.tatp.meowtils.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import wtf.tatp.meowtils.event.api.Event;

/** Normalized client interaction event for block/item-use extensions. */
public final class PlayerInteractEvent extends Event {
    public enum Action { RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR, LEFT_CLICK_BLOCK }
    private final LocalPlayer player;
    private final ClientLevel world;
    private final Action action;
    private final BlockPos pos;
    private final Direction facing;

    public PlayerInteractEvent(LocalPlayer player, ClientLevel world, Action action, BlockPos pos, Direction facing) {
        this.player = player; this.world = world; this.action = action; this.pos = pos; this.facing = facing;
    }
    public LocalPlayer getPlayer() { return player; }
    public ClientLevel getWorld() { return world; }
    public Action getAction() { return action; }
    public BlockPos getPos() { return pos; }
    public Direction getFacing() { return facing; }
}
