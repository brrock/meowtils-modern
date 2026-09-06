package wtf.tatp.meowtils.mixin;

import com.mojang.authlib.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.8 / Via servers send texture properties with {@code signature=""}.
 * Authlib treats any non-null signature as present, then RSA-verify throws and
 * logs on every NPC and player skin. Empty signatures are unsigned, not invalid.
 */
@Mixin(Property.class)
public abstract class PropertySignatureMixin {
    @Inject(method = "hasSignature", at = @At("HEAD"), cancellable = true)
    private void meowtils$emptySignatureIsUnsigned(CallbackInfoReturnable<Boolean> cir) {
        String signature = ((Property) (Object) this).signature();
        if (signature == null || signature.isBlank()) cir.setReturnValue(false);
    }
}
