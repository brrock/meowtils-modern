package wtf.tatp.meowtils.mixin;
import net.minecraft.client.Camera; import org.spongepowered.asm.mixin.Mixin; import org.spongepowered.asm.mixin.injection.At; import org.spongepowered.asm.mixin.injection.Inject; import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable; import wtf.tatp.meowtils.module.utility.ViewClip;
@Mixin(Camera.class) public abstract class CameraMixin { @Inject(method="getMaxZoom",at=@At("HEAD"),cancellable=true) private void disable(float distance,CallbackInfoReturnable<Float> cir){if(ViewClip.active())cir.setReturnValue(distance);} }
