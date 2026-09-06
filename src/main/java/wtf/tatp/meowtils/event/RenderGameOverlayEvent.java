package wtf.tatp.meowtils.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Source-compatible name for the 1.8.9 overlay hook, backed by the 26.2 HUD extractor. */
public final class RenderGameOverlayEvent extends HudRenderEvent {
    public enum ElementType { ALL }
    public RenderGameOverlayEvent(GuiGraphicsExtractor graphics, DeltaTracker delta) { super(graphics, delta); }
    public ElementType getType() { return ElementType.ALL; }
}
