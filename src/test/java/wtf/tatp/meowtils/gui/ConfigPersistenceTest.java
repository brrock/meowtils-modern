package wtf.tatp.meowtils.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import wtf.tatp.meowtils.config.*;
import wtf.tatp.meowtils.gui.values.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigPersistenceTest {
    static final class SavedExtension extends wtf.tatp.meowtils.extension.Extension {
        @Config public boolean enabled;
        @Config public int amount = 3;
        int amountOnEnable;
        SavedExtension() {
            super("Z Saved Extension", "test");
            slider("Amount", 0, 20, 1, null, "amount", int.class);
            expand("Nested", values -> values.addText(new TextValue("Message", "message", this)));
        }
        @Override public void onEnable() { amountOnEnable = amount; }
    }

    @Test void extensionReloadSurvivesInvalidEarlierSetting(@TempDir Path dir) throws Exception {
        Module outdated = new Module("A Outdated", Module.Category.Utility) {};
        outdated.addMode(new ModeValue("Mode", java.util.List.of("Current"), "mode", outdated));
        SavedExtension original = new SavedExtension();
        ModuleManager.register(outdated, original);
        SavedExtension restored = null;
        try {
            ConfigManager.initialize(dir);
            original.amount = 17;
            original.settingsStorage().put("message", "keep this");
            original.setKey(81);
            original.setState(true);
            ConfigManager.save();
            Path file = dir.resolve("meowtils/config.json");
            String json = java.nio.file.Files.readString(file).replace("\"Current\"", "\"Removed mode\"");
            java.nio.file.Files.writeString(file, json);
            ModuleManager.unregister(original);
            restored = new SavedExtension();
            ModuleManager.register(restored);
            ConfigManager.load();
            assertEquals(17, restored.amount);
            assertEquals(17, restored.amountOnEnable);
            assertEquals("keep this", restored.settingsStorage().get("message"));
            assertEquals(81, restored.getKey());
            assertTrue(restored.getState());
            assertTrue(restored.enabled);
        } finally {
            ModuleManager.unregister(outdated);
            ModuleManager.unregister(original);
            if (restored != null) ModuleManager.unregister(restored);
        }
    }

    static final class Example extends Module {
        @Config public boolean checked=true;
        @Config public int count=7, posX=11, posY=12;
        final ToggleValue check=new ToggleValue("Check","checked",this);
        final SliderValue slider=new SliderValue("Count",0,20,1,null,"count",this,int.class);
        final BindValue bind=new BindValue("Bind","binding",this);
        final TextValue text=new TextValue("Text","text",this);
        Example() { super("PersistenceTest",Category.Utility); addToggle(check);addSlider(slider);addBind(bind);addExpand(new ExpandValue("Nested",e->e.addText(text),this)); }
    }
    @Test void roundTripRealFieldsBindingsNestedValuesAndFrames(@TempDir Path dir) {
        Example module=new Example(); ModuleManager.register(module);
        try {
            assertTrue(module.check.get()); assertEquals(7,module.slider.get());
            module.check.set(false); assertFalse(module.checked);
            module.slider.set(9); assertEquals(9,module.count);
            module.bind.setBind(75); module.setKey(76); module.text.set("saved");
            ConfigManager.initialize(dir);
            var frame=ConfigManager.guiConfig.frame("Render",4); frame.x=201; frame.y=81; frame.open=true;
            ConfigManager.save();
            module.check.set(true); module.slider.set(2); module.bind.setBind(0);module.setKey(0);module.text.set("");
            ConfigManager.guiConfig=new GuiConfig(); ConfigManager.load();
            assertFalse(module.check.get());assertEquals(9,module.count);assertEquals(75,module.bind.getBind());assertEquals(76,module.getKey());assertEquals("saved",module.text.get());
            var restored=ConfigManager.guiConfig.frame("Render",4);assertEquals(201,restored.x);assertEquals(81,restored.y);assertTrue(restored.open);
        } finally { ModuleManager.unregister(module); }
    }
}
