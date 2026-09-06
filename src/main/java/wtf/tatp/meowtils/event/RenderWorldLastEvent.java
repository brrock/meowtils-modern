package wtf.tatp.meowtils.event;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

/** Source-compatible name for the old world-last event. */
public final class RenderWorldLastEvent extends WorldRenderEvent {
    public RenderWorldLastEvent(LevelRenderContext context) { super(context); }
}
