package wtf.tatp.meowtils.module.render;

import java.util.List;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.CapeManager;
import wtf.tatp.meowtils.util.Util;

/** Client cape overlay. AvatarRendererMixin swaps the extracted PlayerSkin cape. */
public final class Cape extends Module {
    @Config public String selectedCape = "2011";
    @Config public String customCapeName = "";
    @Config public boolean renderOnAll;

    public Cape() {
        super("Cape", Category.Render);
        addMode(new ModeValue("Cape", List.of("2011", "2012", "2013", "2015", "2016", "Experience", "Founder", "Cobalt", "Astolfo", "Moon", "Myau", "Raven", "Custom"), "selectedCape", this));
        addText(new TextValue("Cape", "File name", "customCapeName", this));
        addToggle(new ToggleValue("Render for all", "renderOnAll", this));
        addButton(new ButtonValue("Cape folder", 5.0f, () -> Util.openFolder(CapeManager.directory(), "cape")));
        tag(ModuleTag.LEGIT);
        tooltip("Renders a cape on players. You may import your own cape file.\n§d/capefolder §f- Open cape folder\n§bText §f- Input the custom cape file name");
    }
}
