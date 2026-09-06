package wtf.tatp.meowtils.module.utility;

import com.mojang.blaze3d.platform.InputConstants;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;

/** Ten independent chat macros; each bind can fire on the same tick. */
public final class AutoText extends Module {
    private final boolean[] held = new boolean[10];

    public AutoText() {
        super("AutoText", Category.Utility);
        addToggle(new ToggleValue("Repeat while held", "repeat", this));
        for (int i = 1; i <= 10; i++) {
            addText(new TextValue(Integer.toString(i), "autoText" + i, this));
            addBind(new BindValue("Bind", "autoText" + i + "Bind", this));
        }
        tooltip("Automatically send a message on key press.");
        tag(ModuleTag.LEGIT);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (mc.gui.screen() != null) return;
        boolean repeat = Settings.bool(this, "repeat", false);
        for (int i = 0; i < 10; i++) {
            int key = Settings.integer(this, "autoText" + (i + 1) + "Bind", 0);
            boolean down = key != 0 && InputConstants.isKeyDown(mc.getWindow(), key);
            if (down && (!held[i] || repeat)) {
                alert(Settings.text(this, "autoText" + (i + 1), ""));
            }
            held[i] = down;
        }
    }

    private static void alert(String msg) {
        if (msg.isEmpty()) Meowtils.addMessage("No message set!");
        else Meowtils.sendCleanMessage(msg);
    }
}
