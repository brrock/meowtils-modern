package wtf.tatp.meowtils.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.module.NoParticles;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void meowtils$filterParticles(net.minecraft.core.particles.ParticleOptions options, double x, double y, double z,
                                          double velocityX, double velocityY, double velocityZ,
                                          CallbackInfoReturnable<Particle> callback) {
        if (NoParticles.filter(options)) callback.setReturnValue(null);
    }
}
