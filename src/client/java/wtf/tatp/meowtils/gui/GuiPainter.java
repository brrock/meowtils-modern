package wtf.tatp.meowtils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import org.joml.Matrix3x2f;

/** Original assets and AWT font atlas, with fractional positions and explicit linear sampling. */
public final class GuiPainter {
    private static final java.util.Map<Integer,LegacyFontAtlas> ATLASES=new java.util.HashMap<>();
    private static Identifier atlasId(int size) { return Identifier.fromNamespaceAndPath("meowtils","dynamic/gui_font_"+size); }
    private static final int[] CHAT_COLORS={0x000000,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    private final GuiGraphicsExtractor g;
    private final Font font;
    public GuiPainter(GuiGraphicsExtractor graphics,Font font) { this.g=graphics;this.font=font; }
    private static LegacyFontAtlas atlas() {
        return atlas(30);
    }
    private static LegacyFontAtlas atlas(int size) {
        return ATLASES.computeIfAbsent(size,key->{
            LegacyFontAtlas baked=new LegacyFontAtlas(size);
            NativeImage pixels=new NativeImage(LegacyFontAtlas.SIZE,LegacyFontAtlas.SIZE,false);
            for (int y=0;y<LegacyFontAtlas.SIZE;y++) for (int x=0;x<LegacyFontAtlas.SIZE;x++) pixels.setPixel(x,y,baked.image.getRGB(x,y));
            Minecraft.getInstance().getTextureManager().register(atlasId(size),new DynamicTexture(()->"Meowtils GUI font",pixels));
            return baked;
        });
    }
    public void texture(String name,int x,int y) { texture(name,x,y,90,20,-1); }
    public void texture(String name,int x,int y,int tint) { texture(name,x,y,90,20,tint); }
    public void texture(String name,int x,int y,int width,int height,int tint) {
        blit(Identifier.fromNamespaceAndPath("meowtils","textures/gui/"+name+".png"),x,y,width,height,0,1,0,1,tint);
    }
    public void texture(Identifier id,int x,int y,int width,int height,int tint) {
        blit(id,x,y,width,height,0,1,0,1,tint);
    }
    private void blit(Identifier id,int x,int y,int width,int height,float u0,float u1,float v0,float v1,int tint) {
        var texture=Minecraft.getInstance().getTextureManager().getTexture(id);
        var sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        g.guiRenderState.addGuiElement(new BlitRenderState(RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(texture.getTextureView(),sampler),new Matrix3x2f(g.pose()),
                x,y,x+width,y+height,u0,u1,v0,v1,tint,g.scissorStack.peek()));
    }
    public float textWidth(String text,float size) { return measure(text,size,font); }
    public static float measure(String text,float size) { return measure(text,size,Minecraft.getInstance().font); }
    public static float measure(String text,float size,Font font) {
        LegacyFontAtlas baked=atlas(); float result=0;
        for (int i=0;i<text.length();) {
            int c=text.codePointAt(i);i+=Character.charCount(c);
            if (c==167 && i<text.length()) { i++;continue; }
            var glyph=baked.glyph(c);
            result+=glyph==null?font.width(new String(Character.toChars(c)))*size/10: glyph.advance()*size/LegacyFontAtlas.FONT_SIZE;
        }
        return result;
    }
    public void text(String text,float x,float baseline,float size,int color) { text(text,x,baseline,size,color,.4f); }
    public void text(String text,float x,float baseline,float size,int color,float shadowOffset) {
        if (text==null || text.isEmpty()) return;
        if (shadowOffset>0) draw(text,x+shadowOffset,baseline+shadowOffset,size,color,true);
        draw(text,x,baseline,size,color,false);
    }
    public void plainText(String text,float x,float baseline,float size,int color) { text(text,x,baseline,size,color,0); }
    private static int shadow(int color) { return color&0xFF000000 | (color&0xFCFCFC)>>2; }
    private void draw(String text,float x,float baseline,float size,int base,boolean shadow) {
        float physicalScale=(float)(Math.hypot(g.pose().m00(),g.pose().m01())*Minecraft.getInstance().getWindow().getGuiScale());
        int rasterSize=LegacyFontAtlas.rasterSize(size*physicalScale);
        LegacyFontAtlas baked=atlas(rasterSize),metrics=atlas(); float ratio=size/baked.fontSize,cursor=0;
        int color=base;
        for (int i=0;i<text.length();) {
            int c=text.codePointAt(i);i+=Character.charCount(c);
            if (c==167 && i<text.length()) {
                char code=Character.toLowerCase(text.charAt(i++));
                if (code=='r') color=base;
                else {
                    int index="0123456789abcdef".indexOf(code);
                    if (index>=0) color=base&0xFF000000 | CHAT_COLORS[index];
                }
                continue;
            }
            int tint=shadow?shadow(color):color;
            var glyph=baked.glyph(c);
            if (glyph==null) {
                String fallback=new String(Character.toChars(c));float s=size/10;
                g.pose().pushMatrix();g.pose().translate(x+cursor,baseline-8*s);g.pose().scale(s,s);
                g.text(font,fallback,0,0,tint,false);g.pose().popMatrix();
                cursor+=font.width(fallback)*s;
            } else {
                if (glyph.width()>0 && glyph.height()>0) {
                    g.pose().pushMatrix();g.pose().translate(x+cursor,baseline);g.pose().scale(ratio,ratio);
                    blit(atlasId(rasterSize),(int)glyph.offsetX(),(int)glyph.offsetY(),glyph.width(),glyph.height(),
                            glyph.x()/1024f,(glyph.x()+glyph.width())/1024f,glyph.y()/1024f,(glyph.y()+glyph.height())/1024f,tint);
                    g.pose().popMatrix();
                }
                cursor+=metrics.glyph(c).advance()*size/LegacyFontAtlas.FONT_SIZE;
            }
        }
    }
}
