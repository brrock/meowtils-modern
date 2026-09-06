package wtf.tatp.meowtils.config;

import com.google.gson.*;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.lang.reflect.Field;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.gui.values.Value;

/** JSON config keyed by module name and the stable config id supplied by extensions. */
public final class ConfigManager {
    private static Path configFile;
    public static GuiConfig guiConfig = new GuiConfig();
    private ConfigManager() {}
    public static void initialize(Path gameDir) {
        if (gameDir != null) wtf.tatp.meowtils.MeowtilsData.use(gameDir);
        wtf.tatp.meowtils.MeowtilsData.ensure();
        configFile = wtf.tatp.meowtils.MeowtilsData.configFile();
        load();
    }
    public static synchronized void load() {
        if (configFile == null || !Files.isRegularFile(configFile)) return;
        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("$gui")) {
                GuiConfig restored = new Gson().fromJson(root.get("$gui"), GuiConfig.class);
                if (restored != null) guiConfig = restored;
            }
            for (Module module : ModuleManager.getModules()) {
                JsonObject data = root.has(module.getName()) ? root.getAsJsonObject(module.getName()) : null;
                if (data == null) continue;
                if (data.has("enabled") && !module.alwaysEnabled) module.setState(data.get("enabled").getAsBoolean());
                loadFields(module, data);
                module.syncLegacyStateFromFields();
                if (data.has("key")) module.setKey(data.get("key").getAsInt());
                for (Object raw : module.getAllValues()) {
                    if (!(raw instanceof Value<?> value) || value.getConfig() == null || !data.has(value.getConfig())) continue;
                    setValue(value, data.get(value.getConfig()));
                }
            }
        } catch (Exception exception) { System.err.println("Meowtils: unable to load config: " + exception); }
    }
    private static void loadFields(Module module, JsonObject data) throws IllegalAccessException {
        for (Class<?> type = module.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                Config annotation = field.getAnnotation(Config.class);
                String key = annotation == null ? null : annotation.value().isBlank() ? field.getName() : annotation.value();
                if (key == null || !data.has(key)) continue;
                field.setAccessible(true); JsonElement json = data.get(key); Class<?> kind = field.getType();
                if (kind == boolean.class || kind == Boolean.class) field.set(module, json.getAsBoolean());
                else if (kind == int.class || kind == Integer.class) field.set(module, json.getAsInt());
                else if (kind == long.class || kind == Long.class) field.set(module, json.getAsLong());
                else if (kind == float.class || kind == Float.class) field.set(module, json.getAsFloat());
                else if (kind == double.class || kind == Double.class) field.set(module, json.getAsDouble());
                else if (kind == String.class) field.set(module, json.getAsString());
            }
        }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setValue(Value value, JsonElement json) {
        Object old = value.getValue();
        if (old instanceof Boolean) value.setValue(json.getAsBoolean());
        else if (old instanceof Double) value.setValue(json.getAsDouble());
        else if (old instanceof Integer) value.setValue(json.getAsInt());
        else if (old instanceof String) value.setValue(json.getAsString());
    }
    public static synchronized void save() {
        if (configFile == null) return;
        JsonObject root = new JsonObject();
        root.add("$gui", new Gson().toJsonTree(guiConfig));
        for (Module module : ModuleManager.getModules()) {
            JsonObject data = new JsonObject(); data.addProperty("enabled", module.getState());
            try { saveFields(module, data); }
            catch (IllegalAccessException exception) { System.err.println("Meowtils: unable to save fields for " + module.getName() + ": " + exception); }
            data.addProperty("key", module.getKey());
            for (Object raw : module.getAllValues()) {
                if (!(raw instanceof Value<?> value) || value.getConfig() == null || value.getValue() == null) continue;
                Object current = value.getValue();
                if (current instanceof Boolean bool) data.addProperty(value.getConfig(), bool);
                else if (current instanceof Number number) data.addProperty(value.getConfig(), number);
                else if (current instanceof String string) data.addProperty(value.getConfig(), string);
            }
            root.add(module.getName(), data);
        }
        try {
            Files.createDirectories(configFile.getParent());
            Path pending = configFile.resolveSibling("config.json.tmp");
            try (Writer writer = Files.newBufferedWriter(pending)) { new GsonBuilder().setPrettyPrinting().create().toJson(root, writer); }
            try { Files.move(pending, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(pending, configFile, StandardCopyOption.REPLACE_EXISTING); }
        } catch (Exception exception) { System.err.println("Meowtils: unable to save config: " + exception); }
    }
    private static void saveFields(Module module, JsonObject data) throws IllegalAccessException {
        for (Class<?> type = module.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                Config annotation = field.getAnnotation(Config.class);
                String key = annotation == null ? null : annotation.value().isBlank() ? field.getName() : annotation.value();
                if (key == null) continue;
                field.setAccessible(true); Object value = field.get(module);
                if (value instanceof Boolean bool) data.addProperty(key, bool);
                else if (value instanceof Number number) data.addProperty(key, number);
                else if (value instanceof String string) data.addProperty(key, string);
            }
        }
    }
}
