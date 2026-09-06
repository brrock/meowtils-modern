package wtf.tatp.meowtils.mixin;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.event.SendPacketEvent;
import wtf.tatp.meowtils.event.api.EventManager;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerMixin {
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void meowtils$beforeSend(Packet<?> packet, CallbackInfo callback) {
        SendPacketEvent event = new SendPacketEvent(packet);
        EventManager.post(event);
        if (event.isCancelled()) callback.cancel();
    }
}
