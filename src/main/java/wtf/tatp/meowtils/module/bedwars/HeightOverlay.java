package wtf.tatp.meowtils.module.bedwars;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;

/** HUD height vs map limit, plus optional wool recolor at/above that limit. */
public final class HeightOverlay extends Module {
    @wtf.tatp.meowtils.config.Config public int red = GUI.BLUE_DEFAULT, green = GUI.BLUE_DEFAULT, blue = GUI.BLUE_DEFAULT;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 1;
    private static Map<String, Integer> mapHeights = new HashMap<>();
    private static boolean scanningMap;
    private static int height = GUI.BLUE_DEFAULT;

    public HeightOverlay() {
        super("HeightOverlay", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Displays map height in HUD and colors wool.");
        ColorLink colorLink = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", colorLink));
        addSaturation(new SaturationValue(colorLink));
        addBrightness(new BrightnessValue(colorLink));
        addToggle(new ToggleValue("Show HUD", "showHud", this));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Show wool overlay", "woolOverlay", this));
        addMode(new ModeValue("Color", List.of("§7Gray", "§8Black", "§6Orange"), "woolColor", this));
        init();
    }

    public static void init() {
        load();
    }

    public static int getHeight() {
        return height;
    }

    public static DyeColor getWoolColor() {
        HeightOverlay module = Module.get(HeightOverlay.class);
        String color = module == null ? "§7Gray" : Settings.text(module, "woolColor", "§7Gray");
        return switch (ColorUtil.plainLower(color)) {
            case "gray", "§7gray" -> DyeColor.LIGHT_GRAY;
            case "black", "§8black" -> DyeColor.BLACK;
            case "orange", "§6orange" -> DyeColor.ORANGE;
            default -> DyeColor.GRAY;
        };
    }

    public static boolean woolOverlayEnabled() {
        HeightOverlay module = Module.get(HeightOverlay.class);
        return module != null && module.getState() && Settings.bool(module, "woolOverlay", true)
                && Server.HYPIXEL.isActive() && Bedwars.GAME.isActive();
    }

    @EventTarget(priority = EventPriority.LOW)
    public void onChatReceived(ChatReceivedEvent event) {
        if (Server.HYPIXEL.isNotActive() || !BedwarsSupport.inMatch()) return;
        String msg = event.getText();
        if (HeightOverlayMap.isMapTrigger(msg)) {
            Meowtils.sendCleanMessage("/map");
            scanningMap = true;
            return;
        }
        if (!scanningMap) return;
        String mapName = HeightOverlayMap.parseMapName(msg);
        if (mapName == null) return;
        height = mapHeights.getOrDefault(mapName.toLowerCase(), GUI.BLUE_DEFAULT);
        scanningMap = false;
        event.setCancelled(true);
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean editor = BedwarsSupport.inEditor();
        if (mc.gui.screen() != null && !editor) return;
        if (Server.HYPIXEL.isNotActive() && !editor) return;
        if (!BedwarsSupport.inMatch() && !editor) return;
        if (Bedwars.PRE_GAME.isActive() && !editor) return;
        if (!Settings.bool(this, "showHud", true)) return;
        String heightColor = mc.player.blockPosition().getY() < height ? "§a" : "§c";
        BedwarsSupport.drawHud(event.getGraphics(),
                "Height: " + heightColor + mc.player.blockPosition().getY() + "§7/§6" + height,
                posX, posY, (float) Settings.number(this, "scale", scale),
                BedwarsSupport.rgb(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue)));
    }

    private static void load() {
        try (InputStream input = HeightOverlay.class.getClassLoader().getResourceAsStream("meowtils/bedwars/bedwars_map_height.json")) {
            if (input == null) {
                Meowtils.error("Failed to find bedwars_map_height.json");
                mapHeights = defaultHeights();
                return;
            }
            mapHeights = new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), new TypeToken<Map<String, Integer>>() {}.getType());
            if (mapHeights == null) mapHeights = defaultHeights();
        } catch (Exception e) {
            mapHeights = defaultHeights();
            e.printStackTrace();
        }
    }

    private static Map<String, Integer> defaultHeights() {
        Map<String, Integer> heights = new HashMap<>();
        for (String name : new String[]{"aquarium","archway","aria","airshow","amazon","apollo","ashfire","biohazard","blossom","cascade","catalyst","chained","crypt","dockyard","dreamgrove","eastwood","gateway","glacier","harvest","hollow","ironclad","kingdom","lighthouse","lotus","mesas","orbit","orchestra","playground","rooftop","speedway","steampunk","treenan","waterfall","yue","zodiac","dragonstar","unturned","sky rise","sky rise ","lectus","boletum","carapace","invasion","lotus","temple","ashore","perimeter","obelisk","cliffside","stonekeep"}) {
            heights.put(name, 86);
        }
        return heights;
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        if (Settings.bool(this, "showHud", true)) {
            return List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                    () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("Height: 99/999", 1, (float) Settings.number(this, "scale", scale))));
        }
        return List.of();
    }

    @Override
    public void onReset() {
        height = GUI.BLUE_DEFAULT;
    }
}
