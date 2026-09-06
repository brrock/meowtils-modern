package wtf.tatp.meowtils.extension.render;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.render.TextureSetup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
/** Source-port bridge only: no OpenGL calls or global renderer state. */
public final class GL11 {
    private static final List<Draw.Point> points=new ArrayList<>();
    private static int mode,color=-1; private static float u,v,lineWidth=1;
    private static boolean textured=true,linear=false; private static Identifier texture;
    public static void reset(){points.clear();color=-1;texture=null;textured=true;linear=false;}
    public static Identifier boundTexture(){return texture;}
    public static void bind(Identifier id){texture=id;}
    public static void glBindTexture(int target,int id){bind(DynamicTexture.identifier(id));}
    public static void glBegin(int kind){mode=kind;points.clear();}
    public static void glVertex2d(double x,double y){points.add(new Draw.Point((float)x,(float)y,u,v,color));}
    public static void glVertex2f(float x,float y){glVertex2d(x,y);}
    public static void glTexCoord2f(float x,float y){u=x;v=y;}
    public static void glEnd(){
        if(Shapes.writingMask()){points.clear();return;}
        List<Draw.Point> quads=new ArrayList<>();
        if(mode==7)quads.addAll(points);
        else if(mode==4){for(int i=0;i+2<points.size();i+=3)Collections.addAll(quads,points.get(i),points.get(i+1),points.get(i+2),points.get(i+2));}
        else if(mode==6||mode==9){for(int i=1;i+1<points.size();i++)Collections.addAll(quads,points.getFirst(),points.get(i),points.get(i+1),points.get(i+1));}
        else if(mode==1){for(int i=0;i+1<points.size();i+=2){var a=points.get(i);var b=points.get(i+1);float dx=b.x()-a.x(),dy=b.y()-a.y(),len=(float)Math.hypot(dx,dy);if(len==0)continue;float ox=-dy/len*lineWidth/2,oy=dx/len*lineWidth/2;quads.add(new Draw.Point(a.x()+ox,a.y()+oy,0,0,a.color()));quads.add(new Draw.Point(b.x()+ox,b.y()+oy,0,0,b.color()));quads.add(new Draw.Point(b.x()-ox,b.y()-oy,0,0,b.color()));quads.add(new Draw.Point(a.x()-ox,a.y()-oy,0,0,a.color()));}}
        boolean useTexture=textured&&texture!=null;
        TextureSetup setup=TextureSetup.noTexture();
        if(useTexture){var t=Minecraft.getInstance().getTextureManager().getTexture(texture);setup=TextureSetup.singleTexture(t.getTextureView(),RenderSystem.getSamplerCache().getClampToEdge(linear?FilterMode.LINEAR:FilterMode.NEAREST));}
        Draw.submit(quads,setup,useTexture);points.clear();
    }
    public static void glColor4f(float r,float g,float b,float a){color=(Math.clamp((int)(a*255),0,255)<<24)|(Math.clamp((int)(r*255),0,255)<<16)|(Math.clamp((int)(g*255),0,255)<<8)|Math.clamp((int)(b*255),0,255);}
    public static void glPushMatrix(){Draw.g().pose().pushMatrix();}
    public static void glPopMatrix(){Draw.g().pose().popMatrix();}
    public static void glTranslatef(float x,float y,float z){Draw.g().pose().translate(x,y);}
    public static void glScalef(float x,float y,float z){Draw.g().pose().scale(x,y);}
    public static void glRotatef(float angle,float x,float y,float z){Draw.g().pose().rotate((float)Math.toRadians(angle));}
    public static void glEnable(int state){if(state==3553)textured=true;}
    public static void glDisable(int state){if(state==3553)textured=false;}
    public static boolean glIsEnabled(int state){return state==3089&&Draw.g().scissorStack.peek()!=null;}
    public static void glLineWidth(float width){lineWidth=width;}
    public static void glTexParameteri(int target,int parameter,int value){
        if(parameter==10240||parameter==10241)linear=value==9729;
    }
    // Blend, alpha and culling are properties of the modern GUI pipelines.
    public static void glBlendFunc(int a,int b){}
    public static void glHint(int a,int b){}
    public static void glShadeModel(int a){}
}
