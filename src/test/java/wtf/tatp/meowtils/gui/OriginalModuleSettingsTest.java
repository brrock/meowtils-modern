package wtf.tatp.meowtils.gui;

import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.module.OriginalModuleSettings;
import wtf.tatp.meowtils.gui.values.*;
import static org.junit.jupiter.api.Assertions.*;

class OriginalModuleSettingsTest {
    @Test void nativeRegistrationAndOriginalSettingsShareTheSameBackingValues() {
        var before=ModuleManager.getModules();
        try {
            wtf.tatp.meowtils.module.RegisterModule.registerAll();
            var original=new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            OriginalModuleSettings.definitions().forEach(raw->original.add(raw.getAsJsonObject().get("name").getAsString()));
            var registered=new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            for (Module module:ModuleManager.getModules()) {
                assertTrue(module.isBehaviorAvailable(), module.getName()+" must be enableable");
                registered.add(module.getName());
            }
            assertEquals(original, registered, "core must register exactly the original 79 names");
            var gui=Module.get(wtf.tatp.meowtils.module.meowtils.GUI.class);
            assertEquals("Auto",gui.scale);
            assertEquals(344,gui.getKey());
            for (Module module:ModuleManager.getModules()) for (Object raw:module.getAllValues()) {
                Value<?> value=(Value<?>)raw;
                assertNotNull(value.getValue(),module.getName()+" / "+value.getName());
            }
            var settings=Module.get(wtf.tatp.meowtils.module.meowtils.Settings.class);
            assertTrue(settings.smoothFont);
            assertEquals(true,settings.settingsStorage().get("smoothFont"));
            ToggleValue smooth=(ToggleValue)settings.getAllValues().stream().filter(v->v instanceof ToggleValue t && "smoothFont".equals(t.getConfig())).findFirst().orElseThrow();
            assertTrue(smooth.get());
            var ping=ModuleManager.find("PingHUD");
            ToggleValue text=(ToggleValue)ping.getAllValues().stream().filter(v->v instanceof ToggleValue t && "text".equals(t.getConfig())).findFirst().orElseThrow();
            text.set(false); assertEquals(false,ping.settingsStorage().get("text"));
        } finally { for (Module m:ModuleManager.getModules()) if (!before.contains(m)) ModuleManager.unregister(m); }
    }
    @Test void everyOriginalDefinitionCanBeConstructedAndReadIncludingNullLabels() {
        var before=ModuleManager.getModules();
        try {
            OriginalModuleSettings.install();
            assertEquals(79,OriginalModuleSettings.definitions().size());
            for (var raw:OriginalModuleSettings.definitions()) {
                var definition=raw.getAsJsonObject();
                Module module=ModuleManager.find(definition.get("name").getAsString());
                assertNotNull(module);
                for (Object setting:module.getAllValues()) {
                    Value<?> value=(Value<?>)setting;
                    assertNotNull(value.getName(),module.getName());
                    assertNotNull(value.getValue(),module.getName()+" / "+value.getName());
                    if (value instanceof ModeValue mode) assertTrue(mode.getModes().contains(mode.getMode()),module.getName()+" / "+mode.getName());
                }
            }
        } finally { for (Module m:ModuleManager.getModules()) if (!before.contains(m)) ModuleManager.unregister(m); }
    }
}
