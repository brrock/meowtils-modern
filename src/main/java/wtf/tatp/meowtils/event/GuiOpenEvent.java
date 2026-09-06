package wtf.tatp.meowtils.event;

import net.minecraft.client.gui.screens.Screen;
import wtf.tatp.meowtils.event.api.Event;

/** Posted when the client GUI screen changes, including a close ({@code gui == null}). */
public final class GuiOpenEvent extends Event {
    private final Screen gui;

    public GuiOpenEvent(Screen gui) {
        this.gui = gui;
    }

    public Screen getGui() {
        return gui;
    }
}
