package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;

/** Tracks iron/gold/diamond/emerald counts in inventory and an open ender chest. */
public final class ResourceTracker extends Module {
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 1;
    private static final String[] TRACKED = {"iron", "gold", "diamond", "emerald"};
    private static final Map<String, Integer> INVENTORY_RESOURCES = new HashMap<>();
    private static final Map<String, Integer> CHEST_RESOURCES = new HashMap<>();
    private static final Map<String, Integer> CURRENT_RESOURCES = new HashMap<>();

    public ResourceTracker() {
        super("ResourceTracker", Category.Bedwars);
        tag(ModuleTag.LEGIT);
        tooltip("Tracks resources in your inventory.");
        addMode(new ModeValue("Display", List.of("Both", "HUD", "Chat"), "mode", this));
        addMode(new ModeValue("Ping sound", List.of("All", "Important", "None"), "soundMode", this));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Stack messages", "stackMessages", this));
        addToggle(new ToggleValue("Hide default message", "hideDefault", this));
        addCheck(new CheckValue("Include §5Enderchest", "enderchest", this));
        addCheck(new CheckValue("Track §7Iron Ingots", "iron", this));
        addCheck(new CheckValue("Track §6Gold Ingots", "gold", this));
        addCheck(new CheckValue("Track §bDiamonds", "diamond", this));
        addCheck(new CheckValue("Track §2Emeralds", "emerald", this));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.gui.screen() != null && !BedwarsSupport.inEditor()) return;
        if (!BedwarsSupport.inMatch() && !BedwarsSupport.inEditor()) return;
        if (Bedwars.PRE_GAME.isActive() && !BedwarsSupport.inEditor()) return;
        if (Settings.text(this, "mode", "Both").equals("Chat")) return;
        float hudScale = (float) Settings.number(this, "scale", scale);
        int x = posX;
        int y = posY;
        var g = event.getGraphics();
        for (String kind : TRACKED) {
            if (!shouldTrack(kind)) continue;
            int inventoryCount = INVENTORY_RESOURCES.getOrDefault(kind, 0);
            int chestCount = CHEST_RESOURCES.getOrDefault(kind, 0);
            String text = String.valueOf(inventoryCount);
            if (Settings.bool(this, "enderchest", true)) {
                text = text + "§8 + " + BedwarsSupport.resourceColor(kind) + chestCount;
            }
            g.pose().pushMatrix();
            g.pose().translate(x, y);
            g.pose().scale(hudScale, hudScale);
            g.item(BedwarsSupport.resourceIcon(kind), 0, 0);
            wtf.tatp.meowtils.font.HudFont.draw(g, BedwarsSupport.resourceColor(kind) + text, 22, 4, 1, 0xFFFFFFFF);
            g.pose().popMatrix();
            y += (int) (12 * hudScale + 6 * hudScale);
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!BedwarsSupport.inMatch() || Bedwars.PRE_GAME.isActive()) return;
        CURRENT_RESOURCES.clear();
        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty()) continue;
            String kind = BedwarsSupport.resourceKey(stack);
            if (kind.isEmpty() || !shouldTrack(kind)) continue;
            CURRENT_RESOURCES.put(kind, CURRENT_RESOURCES.getOrDefault(kind, 0) + stack.getCount());
        }
        checkResourceChange();
        INVENTORY_RESOURCES.clear();
        INVENTORY_RESOURCES.putAll(CURRENT_RESOURCES);
        checkEnderChest();
    }

    @EventTarget(priority = EventPriority.LOWEST)
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay() || !Settings.bool(this, "hideDefault", true)) return;
        if (Bedwars.GAME.isNotActive() || Bedwars.PRE_GAME.isActive()) return;
        String msg = ColorUtil.unformattedText(event.getText());
        if (!msg.startsWith("+")) return;
        String plain = ColorUtil.plainLower(msg);
        if (plain.contains("iron") || plain.contains("gold") || plain.contains("emerald") || plain.contains("diamond")) {
            event.setCancelled(true);
        }
    }

    private void checkResourceChange() {
        String mode = Settings.text(this, "mode", "Both");
        String soundMode = Settings.text(this, "soundMode", "Important");
        for (String kind : TRACKED) {
            if (!shouldTrack(kind)) continue;
            int newCount = CURRENT_RESOURCES.getOrDefault(kind, 0);
            int oldCount = INVENTORY_RESOURCES.getOrDefault(kind, 0);
            if (mode.equals("HUD") || newCount == oldCount) continue;
            String itemName = BedwarsSupport.resourceName(kind);
            boolean gained = newCount > oldCount;
            String prefix = gained ? "§a[+] " : "§c[-] ";
            Meowtils.addMessage(prefix + itemName + " §8(" + newCount + ")");
            if (soundMode.equals("All") || (soundMode.equals("Important") && (kind.equals("diamond") || kind.equals("emerald")))) {
                BedwarsSupport.play(gained ? BedwarsSupport.Sound.LEVEL : BedwarsSupport.Sound.ERROR);
            }
        }
    }

    private void checkEnderChest() {
        if (!(mc.player.containerMenu instanceof ChestMenu chest)) return;
        if (!isEnderChest(chest, BedwarsSupport.screenTitle())) return;
        Map<String, Integer> temp = new HashMap<>();
        for (int i = 0; i < chest.getContainer().getContainerSize(); i++) {
            ItemStack stack = chest.getContainer().getItem(i);
            if (stack.isEmpty()) continue;
            String kind = BedwarsSupport.resourceKey(stack);
            if (kind.isEmpty() || !shouldTrack(kind)) continue;
            temp.put(kind, temp.getOrDefault(kind, 0) + stack.getCount());
        }
        CHEST_RESOURCES.clear();
        CHEST_RESOURCES.putAll(temp);
    }

    private static boolean isEnderChest(ChestMenu chest, String title) {
        if (chest.getContainer() instanceof PlayerEnderChestContainer) return true;
        String plain = ColorUtil.unformattedText(title).trim();
        String lower = ColorUtil.plainLower(plain);
        if (plain.isEmpty()) return false;
        if (lower.contains("ender") && lower.contains("chest")) return true;
        if (lower.equals("container.enderchest")) return true;
        return lower.equals(ColorUtil.plainLower(I18n.get("container.enderchest")));
    }

    private boolean shouldTrack(String kind) {
        return Settings.bool(this, kind, true);
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        return List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("XXX + 99 + 99", 6, (float) Settings.number(this, "scale", scale))));
    }

    @Override
    public void onReset() {
        INVENTORY_RESOURCES.clear();
        CHEST_RESOURCES.clear();
    }
}
