package wtf.tatp.meowtils.module.meowtils;

import java.util.List;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.values.*;

/** Settings and defaults from the 2.0.1 GUI. Keys use GLFW on Fabric. */
public final class GUI extends Module {
    public static final int RED_DEFAULT=189, GREEN_DEFAULT=140, BLUE_DEFAULT=255;
    @Config public int key=344;
    @Config public String scale="Auto", featureMode="Unrestricted";
    @Config public boolean tooltips=true, blurGui=true, debugMode=false, firstStartup=true;
    @Config public int red=RED_DEFAULT, green=GREEN_DEFAULT, blue=BLUE_DEFAULT, scrollSpeed=10;
    public GUI() {
        super("GUI", Category.Meowtils, true);
        setKey(key);
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("GUI color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scroll speed", 1, 25, 1, null, "scrollSpeed", this, int.class));
        addMode(new ModeValue("GUI Scale", List.of("Tiny", "Small", "Normal", "Large", "Huge", "Auto"), "scale", this));
        addMode(new ModeValue("Features", List.of("Unrestricted", "Safe", "Legit"), "featureMode", this));
        addToggle(new ToggleValue("Show tooltips", "tooltips", this));
        addToggle(new ToggleValue("Blur background", "blurGui", this));
        addBind(new BindValue("Bind", "key", this));
        addButton(new ButtonValue("Reset GUI color", 5, () -> {
            red=RED_DEFAULT; green=GREEN_DEFAULT; blue=BLUE_DEFAULT;
            wtf.tatp.meowtils.Meowtils.addMessage("Reset GUI colors!");
        }));
        tooltip("GUI related settings.\n§bMiddle click §f- Bind any module (including this for GUI bind)\n§bScroll wheel §f- Scroll categories/modules if they are too long\n§bFeatures §f- Restrict what type of modules to show");
    }
    public int accent() { return 0xFF000000 | (red & 255) << 16 | (green & 255) << 8 | blue & 255; }
    public static boolean shouldShowModule(Module module) {
        GUI gui=Module.get(GUI.class);
        if (gui == null || module.alwaysEnabled) return true;
        return switch (gui.featureMode) {
            case "Safe" -> module.getTag()==ModuleTag.SAFE || module.getTag()==ModuleTag.LEGIT;
            case "Legit" -> module.getTag()==ModuleTag.LEGIT;
            default -> true;
        };
    }
}
