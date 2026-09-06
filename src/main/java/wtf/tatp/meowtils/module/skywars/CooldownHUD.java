package wtf.tatp.meowtils.module.skywars;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.PlayerInteractEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.bedwars.BedwarsSupport;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

public final class CooldownHUD extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public int red = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public int green = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public int blue = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public float scale = 0.65f;
    @Config public boolean hideAfterCooldown;
    @Config public int posX = 1;
    @Config public int posY = 1;
    private static int cooldown;
    private static boolean ready;
    private static boolean thrownPearl;
    private static int tickCounter;
    private static String lastActionbar = "";
    private static String itemName = "";

    public CooldownHUD() {
        super("CooldownHUD", Category.Skywars);
        tag(ModuleTag.LEGIT);
        tooltip("Display item cooldowns on screen.");
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Text color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addToggle(new ToggleValue("Hide after cooldown", "hideAfterCooldown", this));
    }

    @Override
    public List<HudEntry> hudEditor() {
        return List.of(new HudEntry(null, this, "posX", "posY", () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("Corrupt Pearl: Ready ", 1, (float) Settings.number(this, "scale", scale))));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (!wtf.tatp.meowtils.gui.GuiUtil.inEditor() && !Skywars.GAME.isActive()) return;
        int color = ColorUtil.rgb(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue));
        float drawScale = (float) Settings.number(this, "scale", scale);
        int x = Settings.integer(this, "posX", posX);
        int y = Settings.integer(this, "posY", posY);
        if (wtf.tatp.meowtils.gui.GuiUtil.inEditor()) {
            BedwarsSupport.drawHud(event.getGraphics(), "Corrupt Pearl: §aReady", x, y, drawScale, color);
            return;
        }
        if (itemName.isEmpty()) return;
        if (ready && Settings.bool(this, "hideAfterCooldown", hideAfterCooldown)) return;
        String status = ready ? "§aReady" : thrownPearl ? "§cThrown" : "§c" + cooldown + "s";
        BedwarsSupport.drawHud(event.getGraphics(), itemName + status, x, y, drawScale, color);
    }

    @EventTarget
    public void onChat(ChatReceivedEvent event) {
        String message = event.getText();
        if (event.isOverlay()) {
            lastActionbar = message;
            return;
        }
        if (!ColorUtil.unformattedText(message).equals("The game starts in 1 second!")) return;
        new DelayedTask(this::startFromKit, 20);
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST) return;
        if (cooldown <= 0) return;
        tickCounter++;
        if (tickCounter < 20) return;
        cooldown--;
        tickCounter = 0;
        if (cooldown <= 0) ready = true;
    }

    @EventTarget
    public void onUse(PlayerInteractEvent event) {
        if (mc.player == null || (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) return;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) return;
        if (isCorruptPearl(held) && itemName.equals("Corrupt Pearl: ")) {
            if (ready) {
                thrownPearl = true;
                ready = false;
            }
            return;
        }
        if (isEchoItem(held) && itemName.equals("Echo: ") && ready) {
            ready = false;
            startCooldown("Echo: ", 40);
        }
    }

    private void startFromKit() {
        if (kitMatch("Enderman", "Corrupt Pearl")) startCooldown("Corrupt Pearl: ", 30);
        else if (kitMatch("Enderchest")) startCooldown("Enderchest: ", 60);
        else if (kitMatch("End Lord")) startCooldown("End Lord: ", 30);
        else if (kitMatch("Cryomancer", "Ice Bridge")) startCooldown("Ice Bridge: ", 30);
        else if (kitMatch("Chronobreaker", "Echo")) startCooldown("Echo: ", 8);
    }

    private boolean kitMatch(String... names) {
        for (String name : names) {
            if (textHas(lastActionbar, name) || inventoryHas(name)) return true;
        }
        return false;
    }

    private boolean inventoryHas(String name) {
        if (mc.player == null) return false;
        if (textHasStack(mc.player.getMainHandItem(), name)) return true;
        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (textHasStack(stack, name)) return true;
        }
        return false;
    }

    private void startCooldown(String name, int seconds) {
        itemName = name;
        cooldown = seconds;
        ready = false;
        thrownPearl = false;
        tickCounter = 0;
    }

    private static boolean isCorruptPearl(ItemStack stack) {
        if (!ItemIds.is(stack, "ender_pearl")) return false;
        return stack.hasFoil() || !stack.getEnchantments().isEmpty() || textHasStack(stack, "Corrupt");
    }

    private static boolean isEchoItem(ItemStack stack) {
        return ItemIds.is(stack, "clock") || textHasStack(stack, "Echo") || textHasStack(stack, "Chronobreaker");
    }

    private static boolean textHasStack(ItemStack stack, String needle) {
        if (stack == null || stack.isEmpty()) return false;
        if (textHas(stack.getHoverName().getString(), needle)) return true;
        for (String line : BedwarsSupport.loreLines(stack)) {
            if (textHas(line, needle)) return true;
        }
        return false;
    }

    private static boolean textHas(String text, String needle) {
        if (text == null || needle == null) return false;
        return ColorUtil.unformattedText(text).toLowerCase(java.util.Locale.ROOT)
                .contains(needle.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public void onReset() {
        cooldown = 0;
        ready = false;
        thrownPearl = false;
        tickCounter = 0;
        itemName = "";
        lastActionbar = "";
    }
}
