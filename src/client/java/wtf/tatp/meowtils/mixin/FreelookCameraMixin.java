package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.utility.Freelook;

/** Aims the camera with detached Freelook yaw/pitch, then restores the player look. */
@Mixin(Camera.class)
public abstract class FreelookCameraMixin {
    @Shadow protected abstract void setRotation(float yRot, float xRot);
    @Unique private float meowtils$savedYRot;
    @Unique private float meowtils$savedXRot;
    @Unique private float meowtils$savedYRotO;
    @Unique private float meowtils$savedXRotO;
    @Unique private Entity meowtils$saved;

    @Inject(method = "update", at = @At("HEAD"))
    private void meowtils$freelookHead(DeltaTracker tracker, CallbackInfo callback) {
        meowtils$saved = null;
        if (!Freelook.isActive()) return;
        Entity entity = ((Camera) (Object) this).entity();
        if (entity == null) return;
        meowtils$saved = entity;
        meowtils$savedYRot = entity.getYRot();
        meowtils$savedXRot = entity.getXRot();
        meowtils$savedYRotO = entity.yRotO;
        meowtils$savedXRotO = entity.xRotO;
        entity.setYRot(Freelook.getYaw());
        entity.setXRot(Freelook.getPitch());
        entity.yRotO = Freelook.getYaw();
        entity.xRotO = Freelook.getPitch();
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void meowtils$freelookReturn(DeltaTracker tracker, CallbackInfo callback) {
        if (Freelook.isActive()) setRotation(Freelook.getYaw(), Freelook.getPitch());
        if (meowtils$saved == null) return;
        meowtils$saved.setYRot(meowtils$savedYRot);
        meowtils$saved.setXRot(meowtils$savedXRot);
        meowtils$saved.yRotO = meowtils$savedYRotO;
        meowtils$saved.xRotO = meowtils$savedXRotO;
        meowtils$saved = null;
    }
}
