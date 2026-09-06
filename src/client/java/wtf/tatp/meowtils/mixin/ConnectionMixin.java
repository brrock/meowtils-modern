package wtf.tatp.meowtils.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.module.utility.LatencyAlerts;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void meowtils$beforeReceive(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        if (((Connection) (Object) this).getReceiving() != PacketFlow.CLIENTBOUND) return;
        LatencyAlerts.markPacketReceived();
        ReceivePacketEvent event = new ReceivePacketEvent(packet);
        EventManager.post(event);
        if (event.isCancelled()) callback.cancel();
    }
}
