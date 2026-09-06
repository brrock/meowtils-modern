package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.module.meowtils.Settings;
import wtf.tatp.meowtils.util.ColorUtil;

/** Right-click / middle-click / Ctrl+C copies the hovered chat line when Settings.copyChat is on. */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow protected EditBox input;
    @Shadow private ChatComponent.DisplayMode displayMode;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void meowtils$copyChatClick(MouseButtonEvent event, boolean twice, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 1 && event.button() != 2) return;
        if (meowtils$copyHovered((int) event.x(), (int) event.y())) cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void meowtils$copyChatKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!event.isCopy()) return;
        if (input != null) {
            String highlighted = input.getHighlighted();
            if (highlighted != null && !highlighted.isEmpty()) return;
        }
        Minecraft mc = Minecraft.getInstance();
        var window = mc.getWindow();
        int x = (int) (mc.mouseHandler.xpos() * window.getGuiScaledWidth() / Math.max(1, window.getWidth()));
        int y = (int) (mc.mouseHandler.ypos() * window.getGuiScaledHeight() / Math.max(1, window.getHeight()));
        if (meowtils$copyHovered(x, y)) cir.setReturnValue(true);
    }

    @Unique
    private boolean meowtils$copyHovered(int mouseX, int mouseY) {
        Settings settings = Module.get(Settings.class);
        if (settings == null || !settings.copyChatEnabled()) return false;
        String text = meowtils$hoveredLine(mouseX, mouseY);
        String plain = ColorUtil.unformattedText(text).trim();
        if (plain.isEmpty()) return false;
        Minecraft.getInstance().keyboardHandler.setClipboard(plain);
        Notifications.route("Copied message.", "Meowtils", "Copied chat message", NotificationManager.Type.INFO, 1500L);
        return true;
    }

    @Unique
    private String meowtils$hoveredLine(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null || mc.gui.hud == null) return null;
        ChatComponent chat = mc.gui.hud.getChat();
        if (chat == null || !chat.isChatFocused()) return null;
        Font font = mc.font;
        HoveredLineCollector collector = new HoveredLineCollector(font, mouseX, mouseY);
        ChatComponent.DisplayMode mode = displayMode != null ? displayMode : ChatComponent.DisplayMode.FOREGROUND;
        chat.captureClickableText(collector, mc.getWindow().getGuiScaledHeight(), mc.gui.hud.getGuiTicks(), mode);
        return collector.line();
    }

    @Unique
    private static final class HoveredLineCollector implements ActiveTextCollector {
        private final Font font;
        private final int mouseX;
        private final int mouseY;
        private Parameters parameters = new Parameters(new Matrix3x2f());
        private String line;

        private HoveredLineCollector(Font font, int mouseX, int mouseY) {
            this.font = font;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        @Override public Parameters defaultParameters() { return parameters; }
        @Override public void defaultParameters(Parameters parameters) { this.parameters = parameters; }

        @Override
        public void accept(TextAlignment alignment, int x, int y, Parameters parameters, FormattedCharSequence text) {
            if (line != null) return;
            int left = alignment.calculateLeft(x, font, text);
            var state = new GuiTextRenderState(font, text, parameters.pose(), left, y,
                    ARGB.white(parameters.opacity()), 0, true, true, parameters.scissor());
            ActiveTextCollector.findElementUnderCursor(state, mouseX, mouseY, style -> {
                if (line == null) line = stringify(text);
            });
        }

        @Override
        public void acceptScrolling(Component text, int x, int y, int width, int height, int scroll, Parameters parameters) {
            if (line != null || text == null) return;
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                line = text.getString();
            }
        }

        private String line() { return line; }

        /** Keeps literal {@code §} from Via/1.8 chat; clipboard later strips them. */
        private static String stringify(FormattedCharSequence sequence) {
            StringBuilder out = new StringBuilder();
            sequence.accept((index, style, codePoint) -> {
                out.appendCodePoint(codePoint);
                return true;
            });
            return out.toString();
        }
    }
}
