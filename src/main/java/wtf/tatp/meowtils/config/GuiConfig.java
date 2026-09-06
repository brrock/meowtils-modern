package wtf.tatp.meowtils.config;

import java.util.LinkedHashMap;
import java.util.Map;

/** Coordinates are in original GUI units, independent of Minecraft's GUI scale. */
public final class GuiConfig {
    public final Map<String, FrameState> frames = new LinkedHashMap<>();
    public FrameState frame(String category, int ordinal) {
        return frames.computeIfAbsent(category, ignored -> new FrameState(
                ordinal < 7 ? 5 + ordinal * 85 : 5 + (ordinal - 7) * 85, ordinal < 7 ? 5 : 200, false));
    }
    public static final class FrameState {
        public int x, y;
        public boolean open;
        public FrameState(int x, int y, boolean open) { this.x=x; this.y=y; this.open=open; }
    }
}
