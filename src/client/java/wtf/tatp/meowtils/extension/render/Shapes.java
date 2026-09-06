package wtf.tatp.meowtils.extension.render;
import java.util.*;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
public final class Shapes {
 private static boolean mask; private static float mx,my,mw,mh;
 public static void beginMask(){mask=true;}
 public static void useMask(){mask=false;Draw.g().enableScissor((int)mx,(int)(my-5),(int)Math.ceil(mx+mw),(int)Math.ceil(my+mh));}
 public static void endMask(){Draw.g().disableScissor();}
 public static void round(float x,float y,float w,float h,float r,int c){gradient(x,y,w,h,r,c,c,c,c);}
 public static void gradient(float x,float y,float w,float h,float r,int tl,int tr,int bl,int br){
  if(mask){mx=x;my=y;mw=w;mh=h;return;}
  if(w<=0||h<=0)return;
  mesh(x,y,w,h,r,tl,tr,bl,br,TextureSetup.noTexture(),false);
 }
 public static boolean writingMask(){return mask;}
 private static void mesh(float x,float y,float w,float h,float r,int tl,int tr,int bl,int br,TextureSetup setup,boolean textured){
  r=Math.max(0,Math.min(r,Math.min(w,h)/2));
  List<Draw.Point> points=new ArrayList<>();
  float[] cx={x+r,x+w-r,x+w-r,x+r},cy={y+r,y+r,y+h-r,y+h-r};
  List<Draw.Point> edge=new ArrayList<>();
  for(int corner=0;corner<4;corner++)for(int i=0;i<=12;i++){double angle=Math.toRadians(180+corner*90+i*7.5);float px=cx[corner]+(float)Math.cos(angle)*r,py=cy[corner]+(float)Math.sin(angle)*r;edge.add(point(px,py,x,y,w,h,tl,tr,bl,br));}
  Draw.Point center=point(x+w/2,y+h/2,x,y,w,h,tl,tr,bl,br);
  for(int i=0;i<edge.size();i++){var a=edge.get(i);var b=edge.get((i+1)%edge.size());Collections.addAll(points,center,a,b,b);}
  Draw.submit(points,setup,textured);
 }
 private static Draw.Point point(float px,float py,float x,float y,float w,float h,int tl,int tr,int bl,int br){float u=(px-x)/w,v=(py-y)/h;return new Draw.Point(px,py,u,v,mix(mix(tl,tr,u),mix(bl,br,u),v));}
 public static int mix(int a,int b,float t){int c=0;for(int i=0;i<=24;i+=8)c|=Math.clamp(Math.round(((a>>>i)&255)*(1-t)+((b>>>i)&255)*t),0,255)<<i;return c;}
 public static void outline(float x,float y,float w,float h,float r,float t,int c,int o){round(x-t,y-t,w+2*t,h+2*t,r+t,o);round(x,y,w,h,r,c);}
 public static void texture(int id,float x,float y,float w,float h,float r,float alpha){
  if(id>=0)GL11.glBindTexture(3553,id);
  var identifier=id>=0?DynamicTexture.identifier(id):GL11.boundTexture();if(identifier==null)return;
  var texture=Minecraft.getInstance().getTextureManager().getTexture(identifier);
  int tint=(Math.clamp(Math.round(alpha*255),0,255)<<24)|0xFFFFFF;
  mesh(x,y,w,h,r,tint,tint,tint,tint,TextureSetup.singleTexture(texture.getTextureView(),RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),true);
 }
}
