package meowtils.notifications.tenacity.render;
import java.awt.Color;
import wtf.tatp.meowtils.extension.render.Shapes;
public final class RoundedUtil {
 public static void drawRound(float x,float y,float w,float h,float r,Color c){drawRound(x,y,w,h,r,false,c);}
 public static void drawRound(float x,float y,float w,float h,float r,boolean blur,Color c){Shapes.round(x,y,w,h,r,c.getRGB());}
 public static void drawGradientHorizontal(float x,float y,float w,float h,float r,Color a,Color b){drawGradientRound(x,y,w,h,r,a,a,b,b);}
 public static void drawGradientVertical(float x,float y,float w,float h,float r,Color a,Color b){drawGradientRound(x,y,w,h,r,b,a,b,a);}
 public static void drawGradientCornerLR(float x,float y,float w,float h,float r,Color a,Color b){Color c=ColorUtil.interpolateColorC(a,b,.5f);drawGradientRound(x,y,w,h,r,c,a,b,c);}
 public static void drawGradientCornerRL(float x,float y,float w,float h,float r,Color a,Color b){Color c=ColorUtil.interpolateColorC(a,b,.5f);drawGradientRound(x,y,w,h,r,a,c,c,b);}
 public static void drawGradientRound(float x,float y,float w,float h,float r,Color bl,Color tl,Color br,Color tr){Shapes.gradient(x,y,w,h,r,tl.getRGB(),tr.getRGB(),bl.getRGB(),br.getRGB());}
 public static void drawRoundOutline(float x,float y,float w,float h,float r,float t,Color c,Color o){Shapes.outline(x,y,w,h,r,t,c.getRGB(),o.getRGB());}
 public static void drawRoundTextured(int id,float x,float y,float w,float h,float r,float a){Shapes.texture(id,x,y,w,h,r,a);}
 public static void drawRoundTextured(float x,float y,float w,float h,float r,float a){Shapes.texture(-1,x,y,w,h,r,a);}
}
