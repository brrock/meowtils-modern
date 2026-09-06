package wtf.tatp.meowtils.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import wtf.tatp.meowtils.event.api.Event;

public class WorldRenderEvent extends Event {
    private final LevelRenderContext context;
    public WorldRenderEvent(LevelRenderContext context) { this.context = context; }
    public LevelRenderContext getContext() { return context; }
    public Minecraft getClient() { return Minecraft.getInstance(); }
    public PoseStack getPoseStack() { return context.poseStack(); }
    public SubmitNodeCollector getSubmitNodeCollector() { return context.submitNodeCollector(); }
    public OrderedSubmitNodeCollector getOrderedSubmitNodeCollector() { return context.submitNodeCollector().order(0); }

    /** Submit a camera-relative outline using the backend-neutral 26.2 renderer. */
    public void submitOutline(AABB cameraRelativeBox, int color, float width, boolean afterTerrain) {
        getOrderedSubmitNodeCollector().submitShapeOutline(getPoseStack(), Shapes.create(cameraRelativeBox), RenderTypes.LINES, color, width, afterTerrain);
    }

    /**
     * Camera-relative filled AABB through 26.2 {@link RenderTypes#debugFilledBox()} + {@code submitCustomGeometry}.
     * Same translucent-quad path gizmos use; no raw GL.
     */
    public void submitFilled(AABB cameraRelativeBox, int color) {
        getOrderedSubmitNodeCollector().submitCustomGeometry(getPoseStack(), RenderTypes.debugFilledBox(),
                (pose, consumer) -> emitFilledBox(pose, consumer, cameraRelativeBox, color));
    }

    /** Camera-relative (or pose-local) axis-aligned quad on {@link RenderTypes#debugQuads()}. */
    public void submitBillboardQuad(float x1, float y1, float x2, float y2, int color) {
        float minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        float minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        if (maxX - minX < 1.0e-4f || maxY - minY < 1.0e-4f) return;
        getOrderedSubmitNodeCollector().submitCustomGeometry(getPoseStack(), RenderTypes.debugQuads(),
                (pose, consumer) -> emitQuad(consumer, pose, minX, maxY, 0, maxX, maxY, 0, maxX, minY, 0, minX, minY, 0, color));
    }

    private static void emitFilledBox(PoseStack.Pose pose, VertexConsumer consumer, AABB box, int color) {
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;
        emitQuad(consumer, pose, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color);
        emitQuad(consumer, pose, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, color);
        emitQuad(consumer, pose, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color);
        emitQuad(consumer, pose, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, color);
        emitQuad(consumer, pose, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, color);
        emitQuad(consumer, pose, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    private static void emitQuad(VertexConsumer consumer, PoseStack.Pose pose,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4, int color) {
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x2, y2, z2).setColor(color);
        consumer.addVertex(pose, x3, y3, z3).setColor(color);
        consumer.addVertex(pose, x4, y4, z4).setColor(color);
    }
}
