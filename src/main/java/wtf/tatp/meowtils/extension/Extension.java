package wtf.tatp.meowtils.extension;

import java.util.List;
import java.util.function.Consumer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.gui.values.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import wtf.tatp.meowtils.CommandManager;

/** Base class for portable Meowtils extensions. Keep extension logic in this API. */
public abstract class Extension extends Module {
    private final String author;
    public final Object owner = this;

    protected Extension(String moduleName, String author) {
        super(moduleName, Category.Extensions);
        this.author = author;
        tag(ModuleTag.LEGIT);
    }
    public Object getOwner() { return this; }
    public String getAuthor() { return author; }
    public URL resource(String path) { return ExtensionResources.url(getClass(), path); }
    public InputStream openResource(String path) throws IOException { return ExtensionResources.open(getClass(), path); }
    public void toggle(String name, String config) { addToggle(new ToggleValue(name, config, this)); }
    public void check(String name, String config) { addCheck(new CheckValue(name, config, this)); }
    public void slider(String name, double min, double max, double increment, String valueType, String config, Class<?> targetType) { addSlider(new SliderValue(name, min, max, increment, valueType, config, this, targetType)); }
    public void mode(String name, List<String> modes, String config) { addMode(new ModeValue(name, modes, config, this)); }
    public void text(String name, String config) { addText(new TextValue(name, config, this)); }
    public void text(String name, String description, String config) { addText(new TextValue(name, description, config, this)); }
    public void color(String name, ColorLink link) { addColor(new ColorValue(name, link)); }
    public void saturation(ColorLink link) { addSaturation(new SaturationValue(link)); }
    public void brightness(ColorLink link) { addBrightness(new BrightnessValue(link)); }
    public void opacity(String name, String config) { addOpacity(new OpacityValue(name, config, this)); }
    public void opacity(String name, String config, ColorLink link) { addOpacity(new OpacityValue(name, config, this)); }
    public void button(String name, float textScale, Runnable action) { addButton(new ButtonValue(name, textScale, action)); }
    public void bind(String name, String config) { addBind(new BindValue(name, config, this)); }
    public void expand(String name, Consumer<ExpandValue> builder) { addExpand(new ExpandValue(name, builder, this)); }
    public void info(String name) { tooltip(author == null ? name : name + "\nAuthor: " + author); }
    public ColorLink linkColor(String redConfig, String greenConfig, String blueConfig) {
        SliderValue red = new SliderValue("Red", 0, 255, 1, null, redConfig, this, Integer.class);
        SliderValue green = new SliderValue("Green", 0, 255, 1, null, greenConfig, this, Integer.class);
        SliderValue blue = new SliderValue("Blue", 0, 255, 1, null, blueConfig, this, Integer.class);
        addSlider(red); addSlider(green); addSlider(blue);
        return new ColorLink(red, green, blue);
    }
    public static void registerModule(Module module) {
        if (module == null) throw new IllegalArgumentException("Module cannot be null");
        ModuleManager.register(module);
        ExtensionManager.trackModule(module);
    }
    public static void registerEvent(Object listener) {
        ExtensionManager.trackListener(listener);
    }
    public static void registerCommand(LiteralArgumentBuilder<FabricClientCommandSource> command) { CommandManager.register(command); }
}
