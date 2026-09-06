package wtf.tatp.meowtils.module.hypixel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import wtf.tatp.meowtils.util.ColorUtil;

/** Hypixel nick-book page flattening and nick extraction (Via-tolerant blank lines). */
public final class NickBotBook {
    private NickBotBook() {}

    public static String flattenPageText(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                JsonElement parsed = JsonParser.parseString(trimmed);
                StringBuilder out = new StringBuilder();
                appendJsonComponent(parsed, out);
                if (!out.isEmpty()) return ColorUtil.unformattedText(out.toString());
            } catch (Exception ignored) {
            }
        }
        return ColorUtil.unformattedText(raw);
    }

    public static String extractNick(String text) {
        if (text == null) return null;
        List<String> lines = nonEmptyLines(text);
        if (lines.isEmpty()) return null;

        int useNameIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (isUseName(lines.get(i))) {
                useNameIndex = i;
                break;
            }
        }
        if (useNameIndex > 0) {
            for (int i = useNameIndex - 1; i >= 1; i--) {
                String candidate = lines.get(i);
                if (!isButtonLabel(candidate)) return candidate;
            }
        }

        for (int i = 1; i < lines.size(); i++) {
            String candidate = lines.get(i);
            if (!isButtonLabel(candidate)) return candidate;
        }
        return null;
    }

    private static List<String> nonEmptyLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n", -1)) {
            String clean = ColorUtil.unformattedText(line).trim();
            if (!clean.isEmpty()) lines.add(clean);
        }
        return lines;
    }

    private static boolean isUseName(String line) {
        return line.toUpperCase(Locale.ROOT).contains("USE NAME");
    }

    private static boolean isButtonLabel(String line) {
        String upper = line.toUpperCase(Locale.ROOT);
        return upper.contains("USE NAME") || upper.contains("TRY AGAIN") || upper.startsWith("UH-OH");
    }

    private static void appendJsonComponent(JsonElement element, StringBuilder out) {
        if (element == null || element.isJsonNull()) return;
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) appendJsonComponent(child, out);
            return;
        }
        if (!element.isJsonObject()) {
            if (element.isJsonPrimitive()) out.append(element.getAsString());
            return;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("text") && object.get("text").isJsonPrimitive()) {
            out.append(object.get("text").getAsString());
        }
        if (object.has("extra") && object.get("extra").isJsonArray()) {
            appendJsonArray(object.getAsJsonArray("extra"), out);
        }
        if (object.has("with") && object.get("with").isJsonArray()) {
            appendJsonArray(object.getAsJsonArray("with"), out);
        }
    }

    private static void appendJsonArray(JsonArray array, StringBuilder out) {
        for (JsonElement child : array) appendJsonComponent(child, out);
    }
}
