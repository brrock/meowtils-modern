package wtf.tatp.meowtils.module;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.values.*;

/** Audited 2.0.1 definitions, independent of rendering and game behavior. */
public final class OriginalModuleSettings {
    private OriginalModuleSettings() {}
    public static JsonArray definitions() {
        try (InputStream input=OriginalModuleSettings.class.getResourceAsStream("/meowtils-original-settings.json")) {
            if (input==null) throw new IllegalStateException("Missing original module definitions");
            return JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonArray();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static void install() {
        for (JsonElement raw:definitions()) {
            JsonObject definition=raw.getAsJsonObject(); String name=definition.get("name").getAsString();
            Module module=ModuleManager.find(name);
            boolean unavailable=module==null;
            if (unavailable) {
                module=new DefinitionModule(name,Module.Category.valueOf(definition.get("category").getAsString()));
                module.setPortStatus("Settings restored from 2.0.1; game behavior not yet ported.",false);
            }
            List<Object> existing=module.getAllValues();
            JsonObject defaults=definition.getAsJsonObject("defaults");
            for (var entry:defaults.entrySet()) {
                if (entry.getKey().equals("enabled") || entry.getKey().equals("key")) continue;
                JsonPrimitive p=entry.getValue().getAsJsonPrimitive();
                module.settingsStorage().put(entry.getKey(),p.isBoolean()?p.getAsBoolean():p.isNumber()?p.getAsDouble():p.getAsString());
            }
            Map<String,ColorLink> links=new HashMap<>();
            List<Value<?>> values=build(module,definition.getAsJsonArray("values"),existing,links);
            // Keep real modern-only settings (e.g. Hypixel API key), clearly grouped after the original rows.
            Set<String> configs=new HashSet<>(); collectConfigs(values,configs);
            List<Value<?>> extras=existing.stream().filter(Value.class::isInstance).map(v->(Value<?>)v)
                    .filter(v->!(v instanceof ButtonValue) && !(v instanceof ExpandValue) && v.getConfig()!=null && !configs.contains(v.getConfig())).map(v->(Value<?>)v).collect(java.util.stream.Collectors.toList());
            if (!extras.isEmpty()) {
                Module owner=module;
                values.add(new ExpandValue("Fabric options",e->extras.forEach(e::addValue),owner));
            }
            module.replaceSettings(values);
            module.tooltip(definition.get("tooltip").getAsString());
            if (!definition.get("tag").isJsonNull()) module.tag(Module.ModuleTag.valueOf(definition.get("tag").getAsString()));
            if (unavailable) ModuleManager.register(module);
        }
    }
    private static void collectConfigs(List<Value<?>> values,Set<String> configs) {
        for (Value<?> v:values) {
            if (v.getConfig()!=null) configs.add(v.getConfig());
            if (v instanceof ExpandValue e) collectConfigs(e.getSubValues().stream().map(x->(Value<?>)x).collect(java.util.stream.Collectors.toList()),configs);
        }
    }
    private static List<Value<?>> build(Module owner,JsonArray definitions,List<Object> existing,Map<String,ColorLink> links) {
        List<Value<?>> result=new ArrayList<>();
        for (JsonElement raw:definitions) {
            JsonObject d=raw.getAsJsonObject(); String type=d.get("type").getAsString(),name=string(d,"name","");
            String config=string(d,"config",null);
            ColorLink link=null;
            if (d.has("link")) {
                JsonArray rgb=d.getAsJsonArray("link");
                link=links.computeIfAbsent(rgb.toString(),ignored->new ColorLink(rgb.get(0).getAsString(),rgb.get(1).getAsString(),rgb.get(2).getAsString(),owner));
            }
            Value<?> value=switch(type) {
                case "Toggle" -> new ToggleValue(name,config,boolDefault(d,owner,config),owner);
                case "Check" -> new CheckValue(name,config,owner);
                case "Mode" -> new ModeValue(name,d.getAsJsonArray("modes").asList().stream().map(JsonElement::getAsString).toList(),config,owner);
                case "Slider" -> new SliderValue(name,d.get("min").getAsDouble(),d.get("max").getAsDouble(),d.get("increment").getAsDouble(),d.get("unit").isJsonNull()?null:d.get("unit").getAsString(),config,owner,Double.class);
                case "Text" -> new TextValue(name,string(d,"description",""),config,owner);
                case "Bind" -> new BindValue(name,config,owner);
                case "Color" -> new ColorValue(name,link);
                case "Saturation" -> new SaturationValue(link);
                case "Brightness" -> new BrightnessValue(link);
                case "Opacity" -> new OpacityValue(name,config,owner);
                case "Expand" -> new ExpandValue(name,e->build(owner,d.getAsJsonArray("children"),existing,links).forEach(e::addValue),owner);
                case "Button" -> existing.stream().filter(v->v instanceof ButtonValue b && b.getName().equals(name)).map(v->(ButtonValue)v).findFirst()
                        .orElseGet(()->new ButtonValue(name,d.get("textScale").getAsFloat(),()->wtf.tatp.meowtils.Meowtils.addMessage(owner.getName()+" / "+name+": this action is not yet ported.")));
                default -> throw new IllegalArgumentException("Unknown original control: "+type);
            };
            result.add(value);
        }
        return result;
    }
    private static boolean boolDefault(JsonObject definition,Module owner,String config) {
        if (definition.has("default") && definition.get("default").isJsonPrimitive() && definition.get("default").getAsJsonPrimitive().isBoolean())
            return definition.get("default").getAsBoolean();
        Object stored=config==null?null:owner.settingsStorage().get(config);
        return stored instanceof Boolean flag && flag;
    }
    private static String string(JsonObject object,String key,String fallback) {
        JsonElement value=object.get(key);
        return value==null || value.isJsonNull()?fallback:value.getAsString();
    }
    /** Explicitly unavailable behavior, with the complete real settings definition. */
    private static final class DefinitionModule extends Module {
        DefinitionModule(String name,Category category) { super(name,category); }
    }
}
