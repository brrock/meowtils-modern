package wtf.tatp.meowtils.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.module.RegisterModule;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;
import wtf.tatp.meowtils.module.meowtils.Settings;
import wtf.tatp.meowtils.util.Prefix;

class ChatClicksTest {
    @AfterEach
    void reset() {
        Prefix.resetPrefix();
        for (Module module : ModuleManager.getModules()) ModuleManager.unregister(module);
    }

    @Test
    void defaultPrefixIsTheOriginalMeowTag() {
        RegisterModule.registerAll();
        String prefix = Prefix.getPrefix();
        assertTrue(prefix.contains("M") && prefix.contains("e") && prefix.contains("o") && prefix.contains("w"));
        assertTrue(prefix.contains("[") && prefix.endsWith("] ") || prefix.contains("] "));
        Prefix.setPrefix("TEST ");
        assertTrue("TEST ".equals(Prefix.getPrefix()));
        Prefix.resetPrefix();
        assertTrue(Prefix.getPrefix().contains("M"));
    }

    @Test
    void anticheatColorClicksStayOnTheClient() {
        RegisterModule.registerAll();
        AntiCheat anti = Module.get(AntiCheat.class);
        assertTrue(ChatClicks.handle("/setflagmessagecolor Reason GREEN"));
        assertTrue("GREEN".equals(anti.componentColor));
        assertTrue(ChatClicks.handle("setflagmessagecolor WDR YELLOW"));
        assertTrue("YELLOW".equals(anti.buttonColor));
        assertFalse(ChatClicks.handle("/wdr Steve"));
        assertFalse(ChatClicks.handle("/say hi"));
    }

    @Test
    void themeClicksWriteSettingsPrefixColors() {
        RegisterModule.registerAll();
        Settings settings = Module.get(Settings.class);
        assertTrue(ChatClicks.handle("/settheme m RED"));
        assertTrue("RED".equals(settings.themeM));
    }
}
