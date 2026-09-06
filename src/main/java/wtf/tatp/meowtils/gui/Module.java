package wtf.tatp.meowtils.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.gui.values.*;
import net.minecraft.client.Minecraft;

public abstract class Module {
    public enum Category { Meowtils, Hypixel, Skywars, Bedwars, Render, Antisnipe, Utility, Advanced, Extensions }
    public enum ModuleTag { LEGIT, SAFE, BLATANT }

    private String name;
    private final Category category;
    private final List<Object> values = new ArrayList<>();
    private final ArrayList<ToggleValue> booleans = new ArrayList<>();
    private final ArrayList<CheckValue> checks = new ArrayList<>();
    private final ArrayList<ModeValue> arrays = new ArrayList<>();
    private final ArrayList<TextValue> strings = new ArrayList<>();
    private final ArrayList<SliderValue> sliders = new ArrayList<>();
    private final ArrayList<ColorValue> colors = new ArrayList<>();
    private final ArrayList<SaturationValue> saturation = new ArrayList<>();
    private final ArrayList<BrightnessValue> brightness = new ArrayList<>();
    private final ArrayList<OpacityValue> opacity = new ArrayList<>();
    private final ArrayList<ButtonValue> buttons = new ArrayList<>();
    private final ArrayList<BindValue> binds = new ArrayList<>();
    private final ArrayList<ExpandValue> expands = new ArrayList<>();
    private boolean enabled;
    private int key;
    private boolean keyHeld;
    private String tooltip;
    private ModuleTag tag;
    private final java.util.Map<String,Object> settingsStorage = new java.util.LinkedHashMap<>();
    private String portStatus;
    private boolean behaviorAvailable = true;
    public java.util.Map<String,Object> settingsStorage() { return settingsStorage; }
    public String getPortStatus() { return portStatus; }
    public void setPortStatus(String status, boolean available) { portStatus=status; behaviorAvailable=available; }
    public boolean alwaysEnabled;
    /** Legacy extension field retained for easy 1.8.9-to-modern source ports. */
    protected final Minecraft mc = Minecraft.getInstance();
    protected String moduleName;

    protected Module(String name, Category category) { this(name, category, false); }
    protected Module(String name, Category category, boolean alwaysEnabled) {
        this.name = name; this.moduleName = name; this.category = category; this.alwaysEnabled = alwaysEnabled;
        if (alwaysEnabled) { enabled = true; EventManager.register(this); }
    }
    public void onEnable() {}
    public void onDisable() {}
    public void onReset() {}
    public final void setState(boolean state) {
        if (state && !behaviorAvailable) {
            wtf.tatp.meowtils.Meowtils.addMessage(getName()+": behavior is not ported yet; settings are saved for the port.");
            return;
        }
        if (alwaysEnabled && !state || enabled == state) return;
        enabled = state;
        setLegacyField("enabled", state);
        if (state) { EventManager.register(this); onEnable(); } else { EventManager.unregister(this); onDisable(); }
    }
    public boolean isBehaviorAvailable() { return behaviorAvailable; }
    public boolean getState() { return enabled; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; this.moduleName = name; }
    public Category getCategory() { return category; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; setLegacyField("key", key); }
    public boolean isKeyHeld() { return keyHeld; }
    public void setKeyHeld(boolean keyHeld) { this.keyHeld = keyHeld; }
    public Module tooltip(String text) { tooltip = text; return this; }
    public ModuleTag getTag() { return tag; }
    public Module tag(ModuleTag tag) { this.tag = tag; return this; }
    public String getTooltip() { return tooltip; }
    public List<Object> getOrderedValues() { return Collections.unmodifiableList(values); }
    /** Replace the display definition while retaining the module's native behavior and shared config storage. */
    public void replaceSettings(List<Value<?>> settings) {
        values.clear(); booleans.clear(); checks.clear(); arrays.clear(); strings.clear(); sliders.clear();
        colors.clear(); saturation.clear(); brightness.clear(); opacity.clear(); buttons.clear(); binds.clear(); expands.clear();
        for (Value<?> value:settings) {
            if (value instanceof CheckValue v) addCheck(v);
            else if (value instanceof ToggleValue v) addToggle(v);
            else if (value instanceof OpacityValue v) addOpacity(v);
            else if (value instanceof SliderValue v) addSlider(v);
            else if (value instanceof ColorValue v) addColor(v);
            else if (value instanceof SaturationValue v) addSaturation(v);
            else if (value instanceof BrightnessValue v) addBrightness(v);
            else if (value instanceof ModeValue v) addMode(v);
            else if (value instanceof TextValue v) addText(v);
            else if (value instanceof BindValue v) addBind(v);
            else if (value instanceof ButtonValue v) addButton(v);
            else if (value instanceof ExpandValue v) addExpand(v);
        }
    }
    /** Returns top-level and expanded settings in display/config order. */
    public List<Object> getAllValues() {
        ArrayList<Object> result = new ArrayList<>();
        collectValues(values, result);
        return Collections.unmodifiableList(result);
    }
    private static void collectValues(List<Object> values, List<Object> result) {
        for (Object value : values) {
            result.add(value);
            if (value instanceof ExpandValue expand) collectValues(expand.getSubValues(), result);
        }
    }
    public ArrayList<SliderValue> getValues() { return sliders; }
    public ArrayList<ToggleValue> getBooleans() { return booleans; }
    public ArrayList<CheckValue> getChecks() { return checks; }
    public ArrayList<ModeValue> getArrays() { return arrays; }
    public ArrayList<TextValue> getStrings() { return strings; }
    public ArrayList<ColorValue> getRgb() { return colors; }
    public ArrayList<SaturationValue> getSaturation() { return saturation; }
    public ArrayList<BrightnessValue> getBrightness() { return brightness; }
    public ArrayList<OpacityValue> getOpacity() { return opacity; }
    public ArrayList<ButtonValue> getButton() { return buttons; }
    public ArrayList<BindValue> getBind() { return binds; }
    public ArrayList<ExpandValue> getExpand() { return expands; }
    protected final <T> T addValue(T value) { values.add(value); return value; }
    public void addToggle(ToggleValue value) { booleans.add(value); addValue(value); }
    public void addCheck(CheckValue value) { checks.add(value); addValue(value); }
    public void addMode(ModeValue value) { arrays.add(value); addValue(value); }
    public void addSlider(SliderValue value) { sliders.add(value); addValue(value); }
    public void addText(TextValue value) { strings.add(value); addValue(value); }
    public void addOpacity(OpacityValue value) { opacity.add(value); addValue(value); }
    public void addBind(BindValue value) { binds.add(value); addValue(value); }
    public void addButton(ButtonValue value) { buttons.add(value); addValue(value); }
    public void addExpand(ExpandValue value) { expands.add(value); addValue(value); }
    public void addColor(ColorValue value) { colors.add(value); addValue(value); }
    public void addSaturation(SaturationValue value) { saturation.add(value); addValue(value); }
    public void addBrightness(BrightnessValue value) { brightness.add(value); addValue(value); }
    public void toggle() { setState(!getState()); }
    public void reset() { onReset(); }
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() { return List.of(); }
    public static <T extends Module> T get(Class<T> type) { return type.cast(ModuleManager.getModules().stream().filter(type::isInstance).findFirst().orElse(null)); }
    public static ArrayList<Module> getCategoryModules(Category category) {
        ArrayList<Module> result = new ArrayList<>();
        for (Module module : ModuleManager.getModules()) if (module.getCategory() == category) result.add(module);
        return result;
    }

    /** Copies legacy @Config fields into the runtime state after subclass construction. */
    public final void syncLegacyStateFromFields() {
        Object state = getLegacyField("enabled");
        if (state instanceof Boolean bool) enabled = bool;
        Object keyValue = getLegacyField("key");
        if (keyValue instanceof Number number) key = number.intValue();
    }
    private Object getLegacyField(String name) {
        try {
            var field = getClass().getField(name); field.setAccessible(true); return field.get(this);
        } catch (ReflectiveOperationException ignored) { return null; }
    }
    private void setLegacyField(String name, Object value) {
        try { var field = getClass().getField(name); field.setAccessible(true); field.set(this, value); }
        catch (ReflectiveOperationException ignored) { }
    }
}
