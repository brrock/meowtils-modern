package wtf.tatp.meowtils.mixin;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.extension.render.Blur;
@Mixin(GuiRenderState.class)
public abstract class ExtensionGuiRenderStateMixin {
    @Shadow private int firstStratumAfterBlur;
    @Inject(method="blurBeforeThisStratum",at=@At("HEAD"),cancellable=true)
    private void meowtils$oneBlur(CallbackInfo ci){Blur.request((GuiRenderState)(Object)this);if(firstStratumAfterBlur!=Integer.MAX_VALUE)ci.cancel();}
    @Inject(method="reset",at=@At("HEAD"))
    private void meowtils$resetBlur(CallbackInfo ci){Blur.reset((GuiRenderState)(Object)this);}
}
