package wtf.tatp.meowtils.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/** The original AWT glyph bake, including fractional advances and three-pixel glyph padding. */
public final class LegacyFontAtlas {
    public static final int SIZE=1024;
    public static final float FONT_SIZE=30;
    public final float fontSize;
    public record Glyph(int x,int y,int width,int height,float offsetX,float offsetY,float advance) {}
    public final BufferedImage image=new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_INT_ARGB);
    private final Map<Integer,Glyph> glyphs=new HashMap<>();
    public LegacyFontAtlas() {
        this(FONT_SIZE);
    }
    public LegacyFontAtlas(float fontSize) {
        this.fontSize=fontSize;
        try (var input=LegacyFontAtlas.class.getResourceAsStream("/assets/meowtils/font/gui.ttf")) {
            if (input==null) throw new IOException("Missing Meowtils font");
            Font font=Font.createFont(Font.TRUETYPE_FONT,input).deriveFont(fontSize);
            Graphics2D g=image.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g.setFont(font);g.setColor(Color.WHITE);
                int x=2,y=2,lineHeight=0;
                for (int c=32;c<256;c++) {
                    var vector=font.createGlyphVector(g.getFontRenderContext(),String.valueOf((char)c));
                    Rectangle bounds=vector.getPixelBounds(null,0,0);
                    float advance=vector.getGlyphMetrics(0).getAdvanceX();
                    if (c==' ') { glyphs.put(c,new Glyph(0,0,0,0,0,0,advance));continue; }
                    if (bounds.width<=0 || bounds.height<=0) continue;
                    int w=bounds.width+6,h=bounds.height+6;
                    if (x+w+2>=SIZE) { x=2;y+=lineHeight+2;lineHeight=0; }
                    if (y+h>SIZE) throw new IllegalStateException("Original font atlas overflow");
                    g.drawGlyphVector(vector,x+3-bounds.x,y+3-bounds.y);
                    lineHeight=Math.max(lineHeight,h);
                    glyphs.put(c,new Glyph(x,y,w,h,bounds.x-3,bounds.y-3,advance));
                    x+=w+2;
                }
            } finally { g.dispose(); }
            // Straight-alpha filtering must interpolate white, not black, at glyph edges.
            for (int py=0;py<SIZE;py++) for (int px=0;px<SIZE;px++)
                image.setRGB(px,py,(image.getRGB(px,py)&0xFF000000)|0xFFFFFF);
        } catch (IOException | FontFormatException e) { throw new IllegalStateException("Unable to load original GUI font",e); }
    }
    public Glyph glyph(int codepoint) { return glyphs.get(codepoint); }
    public static int rasterSize(float pixels) {
        int best=12;
        for (int candidate:new int[]{16,20,24,30,36,42,48})
            if (Math.abs(candidate-pixels)<Math.abs(best-pixels)) best=candidate;
        return best;
    }
}
