package wtf.tatp.meowtils.module.hypixel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NickBotBookTest {
    private static final String PLAIN_BOOK =
            "Random Nick\n"
                    + "CoolNick\n"
                    + "USE NAME\n"
                    + "TRY AGAIN";

    private static final String BLANK_LINES =
            "Random Nick\n"
                    + "\n"
                    + "CoolNick\n"
                    + "\n"
                    + "USE NAME\n"
                    + "TRY AGAIN";

    @Test
    void flattenPlainText() {
        assertEquals("Hello world", NickBotBook.flattenPageText("Hello world"));
    }

    @Test
    void flattenJsonObjectWithExtra() {
        String json = "{\"text\":\"Line1\\n\",\"extra\":[{\"text\":\"CoolNick\\n\"},{\"text\":\"USE NAME\"}]}";
        assertEquals("Line1\nCoolNick\nUSE NAME", NickBotBook.flattenPageText(json));
    }

    @Test
    void flattenJsonArrayPages() {
        String json = "[{\"text\":\"Random Nick\\n\"},{\"text\":\"CoolNick\"}]";
        assertEquals("Random Nick\nCoolNick", NickBotBook.flattenPageText(json));
    }

    @Test
    void flattenStripsSectionCodes() {
        assertEquals("CoolNick", NickBotBook.flattenPageText("§aCoolNick"));
    }

    @Test
    void extractNickFromPlainBook() {
        assertEquals("CoolNick", NickBotBook.extractNick(PLAIN_BOOK));
    }

    @Test
    void extractNickSkipsBlankLines() {
        assertEquals("CoolNick", NickBotBook.extractNick(BLANK_LINES));
    }

    @Test
    void extractNickFromFlattenedJsonBook() {
        String json = "{\"text\":\"Random Nick\\n\\n\",\"extra\":[{\"text\":\"CoolNick\\n\\n\"},{\"text\":\"USE NAME\\n\"},{\"text\":\"TRY AGAIN\"}]}";
        String flat = NickBotBook.flattenPageText(json);
        assertEquals("CoolNick", NickBotBook.extractNick(flat));
    }

    @Test
    void extractNickReturnsNullWhenMissing() {
        assertNull(NickBotBook.extractNick("Random Nick\nUSE NAME\nTRY AGAIN"));
        assertNull(NickBotBook.extractNick(null));
    }
}
