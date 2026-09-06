package meowtils.notifications.vape;
import wtf.tatp.meowtils.extension.render.Blur;
import wtf.tatp.meowtils.extension.render.Draw;
public final class VapeBlur {
 public void render(float x,float y,float width,float height,float strength,float radius,int scale){Blur.region(Draw.g(),x,y,width,height,radius);}
}
