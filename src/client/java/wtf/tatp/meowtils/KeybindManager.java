package wtf.tatp.meowtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import wtf.tatp.meowtils.event.KeyPressEvent;
import wtf.tatp.meowtils.event.api.EventManager;

/** Stable keybind registry for modules and extensions. */
public final class KeybindManager {
    private static final List<KeyMapping> mappings = new CopyOnWriteArrayList<>();
    private KeybindManager() {}
    public static KeyMapping register(String id, int defaultKey) {
        KeyMapping mapping = new KeyMapping("key.meowtils." + id, InputConstants.Type.KEYSYM, defaultKey,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MeowtilsClient.MOD_ID, "keybinds")));
        mappings.add(KeyMappingHelper.registerKeyMapping(mapping));
        return mapping;
    }
    public static List<KeyMapping> mappings() { return List.copyOf(mappings); }
    public static void tick() {
        for (KeyMapping mapping : mappings) while (mapping.consumeClick()) EventManager.post(new KeyPressEvent(mapping));
        var client=MeowtilsClient.client();
        if (client==null) return;
        for (var module:wtf.tatp.meowtils.gui.ModuleManager.getModules()) {
            int key=module.getKey();
            boolean down=key>0 && key<=org.lwjgl.glfw.GLFW.GLFW_KEY_LAST && InputConstants.isKeyDown(client.getWindow(),key);
            if (client.gui.screen()==null && client.player!=null && down && !module.isKeyHeld() && !module.alwaysEnabled
                    && !(module instanceof wtf.tatp.meowtils.module.utility.Freelook)) {
                module.toggle(); wtf.tatp.meowtils.config.ConfigManager.save();
            }
            module.setKeyHeld(down);
        }
    }
}
