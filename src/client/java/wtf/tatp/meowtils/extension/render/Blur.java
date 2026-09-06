package wtf.tatp.meowtils.extension.render;

import java.util.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

/** One native blur pass per frame, with the unblurred scene restored outside requested panels. */
public final class Blur {
    private record Region(ScreenRectangle bounds, float radius) {}
    private static final Map<GuiRenderState,List<Region>> regions = new WeakHashMap<>();
    private static final Set<GuiRenderState> full = Collections.newSetFromMap(new WeakHashMap<>());
    private static GuiRenderState requesting;
    private static GpuTexture saved;
    private Blur() {}
    public static void region(GuiGraphicsExtractor g,float x,float y,float width,float height,float radius) {
        if(width<=0 || height<=0)return;
        ScreenRectangle bounds=new ScreenRectangle((int)Math.floor(x),(int)Math.floor(y),(int)Math.ceil(width),(int)Math.ceil(height)).transformMaxBounds(g.pose());
        var clip=g.scissorStack.peek();if(clip!=null)bounds=bounds.intersection(clip);
        if(bounds==null)return;
        boolean first=!regions.containsKey(g.guiRenderState);
        regions.computeIfAbsent(g.guiRenderState,k->new ArrayList<>()).add(new Region(bounds,radius*(float)Math.hypot(g.pose().m00(),g.pose().m01())));
        if(first){requesting=g.guiRenderState;try{g.blurBeforeThisStratum();}finally{requesting=null;}}
    }
    public static void background(GuiGraphicsExtractor g){g.blurBeforeThisStratum();}
    /** Called by the render-state mixin, including vanilla screen requests. */
    public static void request(GuiRenderState state){if(requesting!=state)full.add(state);}
    public static void reset(GuiRenderState state){regions.remove(state);full.remove(state);}
    public static void render(GuiRenderState state,Runnable nativeBlur) {
        var requested=regions.get(state);
        if(full.contains(state)||requested==null||requested.isEmpty()){nativeBlur.run();return;}
        var target=Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture();
        int width=target.getWidth(0),height=target.getHeight(0);
        if(saved==null || saved.getWidth(0)!=width || saved.getHeight(0)!=height || saved.getFormat()!=target.getFormat()){
            if(saved!=null)saved.close();
            saved=RenderSystem.getDevice().createTexture("Meowtils unblurred GUI",GpuTexture.USAGE_COPY_DST|GpuTexture.USAGE_COPY_SRC,target.getFormat(),width,height,1,1);
        }
        var encoder=RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(target,saved,0,0,0,0,0,width,height);
        nativeBlur.run();
        float scale=(float)width/Minecraft.getInstance().getWindow().getGuiScaledWidth();
        // Merge identical scanlines into rectangular GPU copies, including rounded panel corners.
        List<int[]> previous=null;int start=0;
        for(int y=0;y<=height;y++){
            List<int[]> row=y==height?null:outside(requested,width,y,scale);
            if(!same(previous,row)){
                if(previous!=null)for(int[] span:previous)if(span[1]>span[0])encoder.copyTextureToTexture(saved,target,0,span[0],start,span[0],start,span[1]-span[0],y-start);
                previous=row;start=y;
            }
        }
    }
    private static List<int[]> outside(List<Region> regions,int width,int y,float scale){
        List<int[]> covered=new ArrayList<>();
        for(Region region:regions){
            var b=region.bounds;float top=b.top()*scale,bottom=b.bottom()*scale,cy=y+.5f;
            if(cy<top || cy>=bottom)continue;
            float r=Math.max(0,Math.min(region.radius*scale,Math.min(b.width()*scale,b.height()*scale)/2));
            float edge=Math.min(cy-top,bottom-cy),inset=edge<r?r-(float)Math.sqrt(Math.max(0,r*r-(r-edge)*(r-edge))):0;
            covered.add(new int[]{Math.clamp((int)Math.ceil(b.left()*scale+inset),0,width),Math.clamp((int)Math.floor(b.right()*scale-inset),0,width)});
        }
        covered.sort(Comparator.comparingInt(a->a[0]));List<int[]> result=new ArrayList<>();int x=0;
        for(int[] span:covered){if(span[0]>x)result.add(new int[]{x,span[0]});x=Math.max(x,span[1]);}
        if(x<width)result.add(new int[]{x,width});return result;
    }
    private static boolean same(List<int[]> a,List<int[]> b){if(a==null||b==null)return a==b;if(a.size()!=b.size())return false;for(int i=0;i<a.size();i++)if(!Arrays.equals(a.get(i),b.get(i)))return false;return true;}
    public static void close(){if(saved!=null){saved.close();saved=null;}regions.clear();full.clear();}
}
