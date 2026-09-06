package wtf.tatp.meowtils.module.skywars;

import java.util.List;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.util.Settings;

/** Settings-driven dye strip. Mixins/render layers read {@link #active(String)}. */
public final class NoArmorDye extends Module {
    public static NoArmorDye INSTANCE;
    @Config public String mode = "Both";
    public NoArmorDye() {
        super("NoArmorDye", Category.Skywars);
        INSTANCE = this;
        tag(ModuleTag.LEGIT);
        tooltip("Removes dye from leather armor.");
        addMode(new ModeValue("Mode", List.of("Both", "Model", "Item"), "mode", this));
    }
    public static boolean active(String surface) {
        if (INSTANCE == null || !INSTANCE.getState() || wtf.tatp.meowtils.manager.session.Skywars.GAME.isNotActive()) return false;
        String mode = Settings.text(INSTANCE, "mode", "Both");
        return "Both".equals(mode) || mode.equalsIgnoreCase(surface);
    }
    public static boolean shouldStripItem() { return active("Item"); }
    public static boolean shouldStripModel() { return active("Model"); }
}
