package wtf.tatp.meowtils.module.meowtils;

import java.util.List;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

/** Native general-settings module retained for old extension lookups. */
public final class Settings extends Module {
    @wtf.tatp.meowtils.config.Config public String themeM = "WHITE";
    @wtf.tatp.meowtils.config.Config public String themeE = "WHITE";
    @wtf.tatp.meowtils.config.Config public String themeO = "WHITE";
    @wtf.tatp.meowtils.config.Config public String themeW = "WHITE";
    @wtf.tatp.meowtils.config.Config public String themeFirstBracket = "GRAY";
    @wtf.tatp.meowtils.config.Config public String themeSecondBracket = "GRAY";
    @wtf.tatp.meowtils.config.Config public boolean copyChat = true;
    @wtf.tatp.meowtils.config.Config public boolean smoothFont = true;
    @wtf.tatp.meowtils.config.Config public boolean autoUpdate = true;
    private final ModeValue prefix = new ModeValue("Prefix", List.of("Default", "Myau", "Fire", "Nebula", "Air", "Custom", "Short"), "prefix", this);
    public Settings() {
        super("Settings", Category.Meowtils, true);
        addToggle(new ToggleValue("Auto-Updates", "autoUpdate", true, this));
        addToggle(new ToggleValue("Smooth font", "smoothFont", true, this));
        addToggle(new ToggleValue("Copy chat", "copyChat", this));
        addMode(prefix); addCheck(new CheckValue("Lowercase", "lowerCase", this));
        addButton(new ButtonValue("Preview", 1, () -> Meowtils.addMessage(prefix.getMode())));
        syncThemeStorage();
        tooltip("General Meowtils settings.\n§bAuto-Updates §f- Automatically download new updates\n§bSmooth font §f- Toggle smooth font for HUD elements\n§bCopy chat §f- Copy hovered chat message on right click\n§bPrefix §f- Change chat prefix colors\n§d/theme §f- Set custom prefix colors"); tag(ModuleTag.SAFE);
    }
    public boolean copyChatEnabled() {
        return wtf.tatp.meowtils.util.Settings.bool(this, "copyChat", copyChat);
    }
    public void syncThemeStorage() {
        settingsStorage().put("themeM", themeM);
        settingsStorage().put("themeE", themeE);
        settingsStorage().put("themeO", themeO);
        settingsStorage().put("themeW", themeW);
        settingsStorage().put("themeFirstBracket", themeFirstBracket);
        settingsStorage().put("themeSecondBracket", themeSecondBracket);
    }
}
