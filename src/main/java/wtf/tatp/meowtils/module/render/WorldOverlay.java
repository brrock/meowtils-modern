package wtf.tatp.meowtils.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.util.Settings;

/** Camera-relative outlines, filled AABBs, billboards, and HUD projection shared by 26.2 render modules. */
public final class WorldOverlay {
    private WorldOverlay() {}

    public static Vec3 camera() {
        return Minecraft.getInstance().gameRenderer.mainCamera().position();
    }

    public static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static void outline(RenderWorldLastEvent event, AABB world, int color, float width) {
        Vec3 cam = camera();
        event.submitOutline(world.move(-cam.x, -cam.y, -cam.z), color, width, true);
    }

    /** World-space filled AABB via 26.2 {@code RenderTypes.debugFilledBox()} / {@code submitCustomGeometry}. */
    public static void filledBox(RenderWorldLastEvent event, AABB world, int color) {
        Vec3 cam = camera();
        event.submitFilled(world.move(-cam.x, -cam.y, -cam.z), color);
    }

    /** Translucent bed fill with a strong perimeter, both visible through terrain. */
    public static void espBox(RenderWorldLastEvent event, AABB world, int color, boolean fill) {
        if ((color >>> 24) == 0) return;
        Vec3 cam = camera();
        AABB relative = world.move(-cam.x, -cam.y, -cam.z);
        if (fill) event.submitEspFilled(relative, color);
        event.submitEspOutline(relative, color | 0xFF000000, 2.5f);
    }

    /** Alias for other modules / agents that want a {@code submitFilled} call site. */
    public static void submitFilled(RenderWorldLastEvent event, AABB world, int color) {
        filledBox(event, world, color);
    }

    public static boolean wantsFill(Module module, String modeKey, String fallback) {
        if (Settings.bool(module, "fillBox", false)) return true;
        return "Full".equals(Settings.text(module, modeKey, fallback));
    }

    public static void drawBox(RenderWorldLastEvent event, AABB world, int color, boolean fill) {
        if (fill) filledBox(event, world, color);
        else outline(event, world, color, 1.5f);
    }

    /**
     * Original 2.0.1 nametag-style world text: camera-facing, {@code -0.025} scale,
     * see-through. Auto-scale belongs here so perspective keeps size readable.
     */
    public static void worldLabel(RenderWorldLastEvent event, double x, double y, double z, String text, float scale, int color, boolean outline) {
        if (text == null || text.isEmpty()) return;
        Vec3 cam = camera();
        Camera view = Minecraft.getInstance().gameRenderer.mainCamera();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(x - cam.x, y - cam.y, z - cam.z);
        pose.mulPose(Axis.YP.rotationDegrees(-view.yRot()));
        pose.mulPose(Axis.XP.rotationDegrees(view.xRot()));
        float size = -0.025f * Math.max(0.01f, scale);
        pose.scale(size, size, size);
        var font = Minecraft.getInstance().font;
        var sequence = wtf.tatp.meowtils.module.bedwars.BedwarsSupport.legacy(text).getVisualOrderText();
        float left = -font.width(sequence) / 2.0f;
        int packed = 0x00F000F0;
        var collector = event.getOrderedSubmitNodeCollector();
        if (outline) {
            collector.submitText(pose, left + 1, -3, sequence, false, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, packed, 0xFF000000, 0, 0);
            collector.submitText(pose, left - 1, -3, sequence, false, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, packed, 0xFF000000, 0, 0);
            collector.submitText(pose, left, -2, sequence, false, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, packed, 0xFF000000, 0, 0);
            collector.submitText(pose, left, -4, sequence, false, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, packed, 0xFF000000, 0, 0);
        }
        collector.submitText(pose, left, -3, sequence, !outline, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, packed, color | 0xFF000000, 0, 0);
        pose.popPose();
    }

    public static void hudBox(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color, boolean fill) {
        if (fill) graphics.fill(x, y, x + width, y + height, color);
        else hudOutline(graphics, x, y, width, height, color);
    }

    public static boolean projectBox(AABB box, int width, int height, float[] min, float[] max) {
        min[0] = width;
        min[1] = height;
        max[0] = 0;
        max[1] = 0;
        boolean any = false;
        float[] point = new float[2];
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? box.minX : box.maxX;
            double y = (i & 2) == 0 ? box.minY : box.maxY;
            double z = (i & 4) == 0 ? box.minZ : box.maxZ;
            if (!project(x, y, z, width, height, point)) continue;
            any = true;
            min[0] = Math.min(min[0], point[0]);
            min[1] = Math.min(min[1], point[1]);
            max[0] = Math.max(max[0], point[0]);
            max[1] = Math.max(max[1], point[1]);
        }
        return any;
    }

    /** Original 2.0.1 camera-facing 2D entity/block plate (fill or outline). */
    public static void billboard(RenderWorldLastEvent event, AABB world, int color, boolean fill) {
        Vec3 cam = camera();
        double cx = (world.minX + world.maxX) * 0.5 - cam.x;
        double cy = (world.minY + world.maxY) * 0.5 - cam.y;
        double cz = (world.minZ + world.maxZ) * 0.5 - cam.z;
        float halfW = (float) ((world.maxX - world.minX) * 0.5);
        float halfH = (float) ((world.maxY - world.minY) * 0.5);
        Camera view = Minecraft.getInstance().gameRenderer.mainCamera();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(cx, cy, cz);
        pose.mulPose(Axis.YP.rotationDegrees(-view.yRot()));
        pose.mulPose(Axis.XP.rotationDegrees(view.xRot()));
        if (fill) event.submitBillboardQuad(-halfW, -halfH, halfW, halfH, color);
        else event.submitOutline(new AABB(-halfW, -halfH, -0.002, halfW, halfH, 0.002), color, 1.5f, true);
        pose.popPose();
    }

    /** Original Raven-style world-space health bar (yaw-only billboard, scaled GUI rects). */
    public static void healthBar(RenderWorldLastEvent event, double x, double y, double z, float ratio) {
        Vec3 cam = camera();
        Camera view = Minecraft.getInstance().gameRenderer.mainCamera();
        float clamped = Math.max(0.0f, Math.min(1.0f, ratio));
        int barHeight = Math.max(0, Math.min(74, Math.round(74.0f * clamped)));
        int fill = clamped < 0.3f ? 0xFFFF0000 : clamped < 0.5f ? 0xFFFFC800 : clamped < 0.7f ? 0xFFFFFF00 : 0xFF00FF00;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(x - cam.x, y - cam.y, z - cam.z);
        pose.mulPose(Axis.YP.rotationDegrees(-view.yRot()));
        pose.translate(0.0f, 0.0f, -0.12f);
        pose.scale(0.03f, 0.03f, 0.03f);
        event.submitBillboardQuad(20, -1, 26, 75, 0xFF000000, true);
        if (barHeight < 74) event.submitBillboardQuad(21, barHeight, 25, 74, 0xFF404040, true);
        if (barHeight > 0) event.submitBillboardQuad(21, 0, 25, barHeight, fill, true);
        pose.popPose();
    }

    public static AABB interpolated(Entity entity, float partialTick) {
        Vec3 pos = entity.getPosition(partialTick);
        return entity.getBoundingBox().move(pos.x - entity.getX(), pos.y - entity.getY(), pos.z - entity.getZ());
    }

    public static boolean project(double x, double y, double z, int guiWidth, int guiHeight, float[] out) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 pos = camera.position();
        Matrix4f matrix = camera.getViewRotationProjectionMatrix(new Matrix4f());
        Vector4f clip = new Vector4f((float) (x - pos.x), (float) (y - pos.y), (float) (z - pos.z), 1.0f);
        matrix.transform(clip);
        if (clip.w <= 0.05f) return false;
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        if (Math.abs(ndcX) > 1.15f || Math.abs(ndcY) > 1.15f) return false;
        out[0] = (ndcX * 0.5f + 0.5f) * guiWidth;
        out[1] = (1.0f - (ndcY * 0.5f + 0.5f)) * guiHeight;
        return true;
    }

    public static void label(GuiGraphicsExtractor graphics, Font font, String text, float x, float y, float scale, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, -font.width(text) / 2, 0, color, true);
        graphics.pose().popMatrix();
    }

    public static void hudOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    public static float opacity01(Module module, String key, double fallbackPercent) {
        double value = Settings.number(module, key, fallbackPercent);
        return (float) (value > 100.0 ? value / 255.0 : value / 100.0);
    }

    public static int withAlpha(int rgb, float alpha01) {
        int alpha = Math.max(0, Math.min(255, Math.round(alpha01 * 255.0f)));
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public static int color(Module module) {
        return rgb(Settings.integer(module, "red", 255), Settings.integer(module, "green", 255), Settings.integer(module, "blue", 255));
    }
}
