package wtf.tatp.meowtils.module.meowtils;

import java.util.List;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;

/** Configuration surface used by player-list and nametag modules. */
public final class Icons extends Module {
    private final ModeValue display = new ModeValue("Display", List.of("Always", "Tablist", "Nametags"), "display", this);
    public Icons() {
        super("Icons", Category.Meowtils, true);
        addMode(display); addCheck(new CheckValue("Blacklist icons", "blacklistIcon", this));
        addCheck(new CheckValue("Safelist icons", "safelistIcon", this)); addCheck(new CheckValue("Friend icons", "friendIcon", this));
        tooltip("Settings for player icons."); tag(ModuleTag.SAFE);
    }
    public static boolean displayInTab() { Icons icons = Module.get(Icons.class); return icons != null && (icons.display.is("Tablist") || icons.display.is("Always")); }
    public static boolean displayInNametag() { Icons icons = Module.get(Icons.class); return icons != null && (icons.display.is("Nametags") || icons.display.is("Always")); }
}
