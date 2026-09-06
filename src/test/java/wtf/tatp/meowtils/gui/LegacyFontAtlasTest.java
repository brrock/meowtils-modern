package wtf.tatp.meowtils.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegacyFontAtlasTest {
    @Test void screenSizedAtlasesFitAndHaveNoDarkFilteringFringe() {
        for (int size:new int[]{12,16,20,24,30,36,42,48}) {
            LegacyFontAtlas atlas=new LegacyFontAtlas(size);
            assertEquals(size,atlas.fontSize);
            assertNotNull(atlas.glyph('W'));
            assertEquals(0x00FFFFFF,atlas.image.getRGB(0,0));
            assertEquals(size,LegacyFontAtlas.rasterSize(size));
        }
        assertEquals(20,LegacyFontAtlas.rasterSize(19));
    }
    @Test void originalAtlasHasFractionalMetricsAndAntialiasedGlyphs() {
        LegacyFontAtlas atlas=new LegacyFontAtlas();
        assertNotNull(atlas.glyph('A'));assertNotNull(atlas.glyph(' '));
        assertEquals(0,atlas.glyph(' ').width());
        boolean fractional=false,antialiased=false;
        for (int c=33;c<127;c++) {
            var glyph=atlas.glyph(c);assertNotNull(glyph);
            assertTrue(glyph.x()+glyph.width()<LegacyFontAtlas.SIZE);
            assertTrue(glyph.y()+glyph.height()<LegacyFontAtlas.SIZE);
            fractional|=glyph.advance()!=Math.floor(glyph.advance());
            for (int y=glyph.y();y<glyph.y()+glyph.height();y++) for (int x=glyph.x();x<glyph.x()+glyph.width();x++) {
                int alpha=atlas.image.getRGB(x,y)>>>24;
                antialiased|=alpha>0 && alpha<255;
            }
        }
        assertTrue(fractional);assertTrue(antialiased);
    }
}
