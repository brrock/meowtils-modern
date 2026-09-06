package wtf.tatp.meowtils.gui.values;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ColorLink;
public class ExpandValue extends Value<Boolean> {
    private final List<Object> subValues = new ArrayList<>();
    private final Module owner;
    public ExpandValue(String name, Consumer<ExpandValue> builder, Module owner) { super(name, name, false); this.owner = owner; builder.accept(this); }
    public void addExpand(ExpandValue value) { subValues.add(value); }
    public void addValue(Value<?> value) { subValues.add(value); }
    public List<Object> getSubValues() { return Collections.unmodifiableList(subValues); }
    public boolean getState() { return getValue(); }
    public void setState(boolean state) { setValue(state); }
    public void toggle() { setState(!getState()); }
    public void addToggle(ToggleValue value) { subValues.add(value); }
    public void addCheck(CheckValue value) { subValues.add(value); }
    public void addSlider(SliderValue value) { subValues.add(value); }
    public void addMode(ModeValue value) { subValues.add(value); }
    public void addText(TextValue value) { subValues.add(value); }
    public void addColor(ColorValue value) { subValues.add(value); }
    public void addSaturation(SaturationValue value) { subValues.add(value); }
    public void addBrightness(BrightnessValue value) { subValues.add(value); }
    public void addOpacity(OpacityValue value) { subValues.add(value); }
    public void addBind(BindValue value) { subValues.add(value); }
    public void addButton(ButtonValue value) { subValues.add(value); }
    public void toggle(String name, String config) { addToggle(new ToggleValue(name, config, null)); }
    public void check(String name, String config) { addCheck(new CheckValue(name, config, null)); }
    public void slider(String name, double min, double max, double increment, String type, String config, Class<?> target) { addSlider(new SliderValue(name, min, max, increment, type, config, null, target)); }
    public void mode(String name, List<String> modes, String config) { addMode(new ModeValue(name, modes, config, null)); }
    public void text(String name, String config) { addText(new TextValue(name, config, null)); }
    public void text(String name, String description, String config) { addText(new TextValue(name, description, config, null)); }
    public void color(String name, ColorLink link) { addColor(new ColorValue(name, link)); }
    public void saturation(ColorLink link) { addSaturation(new SaturationValue(link)); }
    public void brightness(ColorLink link) { addBrightness(new BrightnessValue(link)); }
    public void opacity(String name, String config) { addOpacity(new OpacityValue(name, config, null)); }
    public void button(String name, float scale, Runnable action) { addButton(new ButtonValue(name, scale, action)); }
    public void bind(String name, String config) { addBind(new BindValue(name, config, null)); }
}
