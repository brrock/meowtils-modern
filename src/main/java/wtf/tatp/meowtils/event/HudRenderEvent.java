package wtf.tatp.meowtils.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.event.api.Event;

public class HudRenderEvent extends Event {
    private final GuiGraphicsExtractor graphics;
    private final DeltaTracker delta;
    public HudRenderEvent(GuiGraphicsExtractor graphics, DeltaTracker delta) { this.graphics = graphics; this.delta = delta; }
    public GuiGraphicsExtractor getGraphics() { return graphics; }
    public DeltaTracker getDelta() { return delta; }
    public Minecraft getClient() { return Minecraft.getInstance(); }
}
