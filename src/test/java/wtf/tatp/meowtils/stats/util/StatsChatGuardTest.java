package wtf.tatp.meowtils.stats.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.module.RegisterModule;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.util.Prefix;

class StatsChatGuardTest {
    @AfterEach
    void reset() {
        StatsChatGuard.reset();
        Prefix.resetPrefix();
        for (Module module : ModuleManager.getModules()) ModuleManager.unregister(module);
    }

    @Test
    void compactStatsLineIsOwnOutput() {
        RegisterModule.registerAll();
        assertTrue(StatsChatGuard.isOwnLine("[Meow] [0✫]  Missed | FKDR: 0.0 | WLR: 0.0"));
        assertTrue(StatsChatGuard.isOwnLine("[Meow] [0✫]  Saved | FKDR: 0.0 | WLR: 0.0"));
        assertTrue(StatsChatGuard.isOwnLine(Prefix.getPrefix() + "hello"));
        assertFalse(StatsChatGuard.isOwnLine("Steve: gg"));
        assertFalse(StatsChatGuard.isOwnLine("[MVP+] Steve: waiting in bedwars"));
    }

    @Test
    void compactLineWouldOtherwiseLookLikeAPlayerName() {
        var names = Stats.namesIn("[Meow] [0✫]  Missed | FKDR: 0.0 | WLR: 0.0", "[Meow] [0✫]  Missed | FKDR: 0.0 | WLR: 0.0");
        assertTrue(names.contains("Missed"));
    }

    @Test
    void urchinKeyIsEmptyUntilConfigured() {
        RegisterModule.registerAll();
        assertTrue(Stats.urchinKey().isEmpty());
    }

    @Test
    void autoCheckDoesNotReprintTheSameName() {
        assertFalse(StatsChatGuard.alreadyShown("Missed", "bw-c"));
        assertTrue(StatsChatGuard.alreadyShown("Missed", "bw-c"));
        assertFalse(StatsChatGuard.alreadyShown("Saved", "bw-c"));
    }
}
