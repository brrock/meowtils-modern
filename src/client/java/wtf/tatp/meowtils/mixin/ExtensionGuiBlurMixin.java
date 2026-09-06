package wtf.tatp.meowtils.mixin;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import wtf.tatp.meowtils.extension.render.Blur;
@Mixin(GuiRenderer.class)
public abstract class ExtensionGuiBlurMixin {
    @Shadow @Final private GuiRenderState renderState;
    @WrapOperation(method="draw",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/GameRenderer;processBlurEffect()V"))
    private void meowtils$regionalBlur(GameRenderer renderer,Operation<Void> original){Blur.render(renderState,()->original.call(renderer));}
}
