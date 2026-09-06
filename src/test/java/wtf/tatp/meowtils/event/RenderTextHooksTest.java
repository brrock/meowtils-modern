package wtf.tatp.meowtils.event;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.event.api.*;
import wtf.tatp.meowtils.util.RenderTextHooks;
import static org.junit.jupiter.api.Assertions.*;

class RenderTextHooksTest {
    static final class Replacer {
        @EventTarget public void text(RenderStringEvent event) {
            event.setString(event.getString().replace("Level: 5", "Level: §a100"));
        }
    }
    @Test void dispatchesWithoutAccountHider() {
        Replacer listener = new Replacer(); EventManager.register(listener);
        try { assertEquals("Level: §a100", RenderTextHooks.apply("Level: 5")); }
        finally { EventManager.unregister(listener); }
        assertFalse(EventManager.hasListeners(RenderStringEvent.class));
    }
    @Test void unchangedStyledTextRetainsOriginalSequence() {
        Replacer listener = new Replacer(); EventManager.register(listener);
        try {
            var input=FormattedCharSequence.forward("No changes",Style.EMPTY.withColor(0x123456).withBold(true));
            assertSame(input,RenderTextHooks.apply(input));
        } finally { EventManager.unregister(listener); }
    }
    @Test void styledTextReplacementRetainsFormattingAndCustomColor() {
        Replacer listener = new Replacer(); EventManager.register(listener);
        try {
            var input=FormattedCharSequence.forward("Level: 5",Style.EMPTY.withColor(0x123456));
            StringBuilder output=new StringBuilder();
            RenderTextHooks.apply(input).accept((index,style,codepoint)->{
                assertEquals(output.length()<7?0x123456:0x55FF55,style.getColor().getValue());
                output.appendCodePoint(codepoint);return true;
            });
            assertEquals("Level: 100",output.toString());
        } finally { EventManager.unregister(listener); }
    }
}
