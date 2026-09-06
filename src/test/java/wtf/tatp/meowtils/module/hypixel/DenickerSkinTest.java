package wtf.tatp.meowtils.module.hypixel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DenickerSkinTest {
    private static final String SAMPLE_HASH = "4c7b0468044bfecacc43d00a3a69335a834b73937688292c20d3988cae58248d";
    private static final String SAMPLE_URL = "https://textures.minecraft.net/texture/" + SAMPLE_HASH;

    @Test
    void decodesMojangTexturesPayload() {
        String json = """
                {
                  "timestamp": 1427844249898,
                  "profileId": "8667ba71b85a4004af457ef819755791",
                  "profileName": "Notch",
                  "textures": {
                    "SKIN": {
                      "url": "%s"
                    }
                  }
                }
                """.formatted(SAMPLE_URL);
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        DenickerSkin.Data data = DenickerSkin.fromTexturesBase64(base64);

        assertEquals(SAMPLE_HASH, data.hash());
        assertEquals("Notch", data.profileName());
    }

    @Test
    void extractsHashFromTexturesUrl() {
        assertEquals(SAMPLE_HASH, DenickerSkin.hashFromUrl(SAMPLE_URL));
        assertEquals(SAMPLE_HASH, DenickerSkin.hashFromUrl("http://textures.minecraft.net/texture/" + SAMPLE_HASH));
    }

    @Test
    void rejectsInvalidInputs() {
        assertNull(DenickerSkin.fromTexturesBase64(""));
        assertNull(DenickerSkin.fromTexturesBase64("not-base64"));
        assertNull(DenickerSkin.hashFromUrl(""));
        assertNull(DenickerSkin.hashFromUrl("https://textures.minecraft.net/texture/"));
    }
}
