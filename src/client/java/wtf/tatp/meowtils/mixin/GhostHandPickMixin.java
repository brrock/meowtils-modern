package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.module.advanced.GhostHand;

/** Re-picks through ghost-hand entities so the crosshair lands on the block or next entity. */
@Mixin(Minecraft.class)
public abstract class GhostHandPickMixin {
    @Inject(method = "pick", at = @At("RETURN"))
    private void meowtils$ghostHand(float partialTick, CallbackInfo callback) {
        Minecraft self = (Minecraft) (Object) this;
        if (!(self.hitResult instanceof EntityHitResult entityHit)) return;
        if (!GhostHand.shouldPassThrough(entityHit.getEntity())) return;
        Entity camera = self.getCameraEntity();
        if (camera == null || self.player == null || self.level == null) return;
        double blockRange = self.player.blockInteractionRange();
        double entityRange = self.player.entityInteractionRange();
        double reach = Math.max(blockRange, entityRange);
        HitResult blockHit = camera.pick(blockRange, partialTick, false);
        Vec3 eye = camera.getEyePosition(partialTick);
        Vec3 look = camera.getViewVector(partialTick);
        Vec3 end = eye.add(look.scale(reach));
        EntityHitResult nextEntity = ProjectileUtil.getEntityHitResult(
                camera,
                eye,
                end,
                camera.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0),
                entity -> entity != entityHit.getEntity()
                        && entity.isPickable()
                        && !entity.isSpectator()
                        && !GhostHand.shouldPassThrough(entity),
                reach * reach);
        HitResult chosen = closer(eye, blockHit, nextEntity);
        self.hitResult = chosen;
        self.crosshairPickEntity = chosen instanceof EntityHitResult next ? next.getEntity() : null;
    }

    private static HitResult closer(Vec3 eye, HitResult blockHit, EntityHitResult entityHit) {
        if (entityHit == null) return blockHit;
        if (blockHit == null || blockHit.getType() == HitResult.Type.MISS) return entityHit;
        if (eye.distanceToSqr(entityHit.getLocation()) < eye.distanceToSqr(blockHit.getLocation())) return entityHit;
        return blockHit;
    }
}
