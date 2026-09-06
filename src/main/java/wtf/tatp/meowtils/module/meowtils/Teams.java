package wtf.tatp.meowtils.module.meowtils;

import java.util.List;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

/** Shared team/friend filtering settings used by combat and ESP modules. */
public final class Teams extends Module {
    private final ModeValue bots = new ModeValue("Ignore bots", List.of("Hypixel", "Universal", "Dynamic", "None"), "ignoreBotMode", this);
    private final ToggleValue team = new ToggleValue("Ignore team", "ignoreTeam", this);
    private final ToggleValue friends = new ToggleValue("Ignore friends", "ignoreFriends", this);
    public Teams() { super("Teams", Category.Meowtils, true); addMode(bots); addToggle(team); addToggle(friends); tooltip("Makes modules ignore teammates."); tag(ModuleTag.SAFE); }
    public static boolean ignoreTeam() { Teams value = Module.get(Teams.class); return value == null || value.team.get(); }
}
