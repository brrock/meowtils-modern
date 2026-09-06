package wtf.tatp.meowtils.gui;

import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.gui.values.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GuiInteractionTest {
    @Test void sliderTracksMouseClampsAndRounds() {
        SliderValue slider=new SliderValue("Scale",.5,1.5,.05,null,"scale",null,float.class);
        GuiInteraction.drag(slider,41,4,74); assertEquals(1,slider.get(),.001);
        GuiInteraction.drag(slider,999,4,74); assertEquals(1.5,slider.get());
        GuiInteraction.drag(slider,-99,4,74); assertEquals(.5,slider.get());
        slider.setValue(.731); assertEquals(.75,slider.get(),.001);
    }
    @Test void nestedExpansionHidesDescendantsWithoutLosingValues() {
        ToggleValue toggle=new ToggleValue("Enabled","enabled",null); toggle.set(true);
        ExpandValue inner=new ExpandValue("Inner",e->e.addToggle(toggle),null);
        ExpandValue outer=new ExpandValue("Outer",e->e.addExpand(inner),null);
        assertEquals(List.of(outer),GuiInteraction.visibleValues(List.of(outer)));
        outer.setState(true); inner.setState(true);
        assertEquals(List.of(outer,inner,toggle),GuiInteraction.visibleValues(List.of(outer)));
        outer.setState(false); assertTrue(toggle.get());
    }
    @Test void scaleIsIndependentOfMinecraftUiScale() {
        assertEquals(1,GuiInteraction.scale("Normal",1920,3));
        assertEquals(.6,GuiInteraction.scale("Tiny",1920,4),.0001);
        assertEquals(1,GuiInteraction.scale("Auto",1280,2));
    }
    @Test void textInputFiltersControlsAndPreservesUnicode() {
        assertEquals("hello world",GuiInteraction.appendText("hello ","world\n\t"));
        assertEquals(256,GuiInteraction.appendText("x".repeat(255),"ab").length());
        assertEquals("hello",GuiInteraction.backspace("hello😀"));
        assertEquals("",GuiInteraction.backspace(""));
    }
    @Test void colorControlsShareRgbAndRetainHueThroughBlack() {
        ColorLink link=new ColorLink(new SliderValue("R",0,255,1,null,"r",null,int.class),new SliderValue("G",0,255,1,null,"g",null,int.class),new SliderValue("B",0,255,1,null,"b",null,int.class));
        link.setRGB(0xFF0000);
        ColorValue hue=new ColorValue("Color",link); BrightnessValue brightness=new BrightnessValue(link);
        GuiInteraction.drag(hue,50,0,100);
        assertEquals(0x00FFFF,link.getRGB());
        brightness.set(0); assertEquals(0,link.getRGB());
        brightness.set(100); assertEquals(0x00FFFF,link.getRGB());
        new SaturationValue(link).set(0); assertEquals(0xFFFFFF,link.getRGB());
    }
}
