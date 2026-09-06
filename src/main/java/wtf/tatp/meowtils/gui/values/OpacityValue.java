package wtf.tatp.meowtils.gui.values;
import wtf.tatp.meowtils.gui.Module;
public class OpacityValue extends SliderValue { public OpacityValue(String name, String config, Module owner) { super(name, 0, 255, 1, null, config, owner, Integer.class); } }
