package wtf.tatp.meowtils.extension.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3x2f;
import java.util.List;

/** Captures the original UI geometry into the Minecraft 26.2 extraction pipeline. */
public final class Draw {
    private static GuiGraphicsExtractor graphics;
    public static GuiGraphicsExtractor g() { return java.util.Objects.requireNonNull(graphics, "Outside UI extraction"); }
    public static void begin(GuiGraphicsExtractor g) { graphics=g; GL11.reset(); }
    public static void end() { graphics=null; }
    public record Point(float x,float y,float u,float v,int color) {}
    public static void submit(List<Point> points,TextureSetup texture,boolean textured) {
        if(points.isEmpty()) return;
        Matrix3x2f pose=new Matrix3x2f(g().pose());
        ScreenRectangle clip=g().scissorStack.peek();
        float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;
        for(Point p:points) { float x=pose.m00()*p.x+pose.m10()*p.y+pose.m20(), y=pose.m01()*p.x+pose.m11()*p.y+pose.m21(); minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y); }
        ScreenRectangle bounds=new ScreenRectangle((int)Math.floor(minX),(int)Math.floor(minY),(int)Math.ceil(maxX)-(int)Math.floor(minX)+1,(int)Math.ceil(maxY)-(int)Math.floor(minY)+1);
        if(clip!=null) bounds=bounds.intersection(clip);
        if(bounds==null) return;
        g().guiRenderState.addGuiElement(new Geometry(List.copyOf(points),pose,texture,textured,clip,bounds));
    }
    private record Geometry(List<Point> points,Matrix3x2f pose,TextureSetup textureSetup,boolean textured,ScreenRectangle scissorArea,ScreenRectangle bounds) implements GuiElementRenderState {
        public RenderPipeline pipeline() { return textured?RenderPipelines.GUI_TEXTURED:RenderPipelines.GUI; }
        public void buildVertices(VertexConsumer consumer) {
            for(Point p:points) { var vertex=consumer.addVertexWith2DPose(pose,p.x,p.y).setColor(p.color);if(textured)vertex.setUv(p.u,p.v); }
        }
    }
}
