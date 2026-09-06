package wtf.tatp.meowtils.module.advanced;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.GuiOpenEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.advanced.AutoChestGate.Kind;
import wtf.tatp.meowtils.module.bedwars.BedwarsSupport;
import wtf.tatp.meowtils.module.bedwars.ShopHelper;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.Settings;

/** Shift-clicks bedwars resources into or out of a chest on a client-side delay. */
public final class AutoChest extends Module {
    private static final String LOCAL_CHEST = I18n.get("container.chest");
    private static final String LOCAL_ENDER_CHEST = I18n.get("container.enderchest");

    public static final Set<Integer> CLICKED_SLOTS = new HashSet<>();
    private static long nextClickTime;
    private static boolean shouldDeposit;
    private static boolean shouldTake;
    private static boolean shouldDump;
    private static int lastCheckedSlot;

    public AutoChest() {
        super("AutoChest", Category.Advanced);
        tag(ModuleTag.BLATANT);
        tooltip("Automatically deposit resources into a chest. Only works in bedwars.\n§cWARNING: §cLow §cdelays §care §cdetectable!");
        addSlider(new SliderValue("Delay", 0, 500, 50, "ms", "delay", this, Integer.class));
        addSlider(new SliderValue("Wait delay", 0, 500, 50, "ms", "waitDelay", this, Integer.class));
        addBind(new BindValue("Dump", "dumpKey", this));
        addBind(new BindValue("Take", "takeKey", this));
        addToggle(new ToggleValue("Render clicked", "renderClicked", this));
        addToggle(new ToggleValue("Allow for §5Enderchest", "enderChests", this));
        addToggle(new ToggleValue("Allow for §eChest", "normalChests", this));
        addCheck(new CheckValue("§7Iron Ingots", "iron", this));
        addCheck(new CheckValue("§6Gold Ingots", "gold", this));
        addCheck(new CheckValue("§bDiamonds", "diamonds", this));
        addCheck(new CheckValue("§2Emeralds", "emeralds", this));
    }

    @EventTarget
    public void onGuiOpen(GuiOpenEvent event) {
        if (Bedwars.ALL.isNotActive()) return;
        if (!(event.getGui() instanceof ContainerScreen screen)) {
            disable();
            return;
        }
        if (!(screen.getMenu() instanceof ChestMenu menu)) {
            disable();
            return;
        }
        if (!allowedChest(screen, menu)) return;
        int openDelay = Settings.integer(this, "waitDelay", 100) / 50;
        new DelayedTask(() -> {
            shouldDeposit = true;
            lastCheckedSlot = 0;
        }, openDelay);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.PRE || mc.player == null || mc.level == null) return;
        if (!(mc.gui.screen() instanceof ContainerScreen) || Bedwars.ALL.isNotActive()) return;
        if ((mc.mouseHandler.isLeftPressed() || mc.mouseHandler.isRightPressed() || mc.mouseHandler.isMiddlePressed())
                && (shouldDump || shouldTake || shouldDeposit)) {
            Meowtils.addMessage(ChatFormatting.RED + "You can't click while AutoChest is currently active, disabling.");
            disable();
        }
        int dumpKey = Settings.integer(this, "dumpKey", 0);
        if (dumpKey != 0 && InputConstants.isKeyDown(mc.getWindow(), dumpKey)) {
            shouldDump = true;
            lastCheckedSlot = 0;
        }
        int takeKey = Settings.integer(this, "takeKey", 0);
        if (takeKey != 0 && InputConstants.isKeyDown(mc.getWindow(), takeKey)) {
            shouldTake = true;
            lastCheckedSlot = 0;
        }
        if (Settings.bool(this, "renderClicked", true)) CLICKED_SLOTS.clear();
        if (!shouldDeposit && !shouldTake && !shouldDump) return;
        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;
        nextClickTime = now + Settings.integer(this, "delay", 100);
        if (!activate((AbstractContainerScreen<?>) mc.gui.screen())) disable();
    }

    private boolean activate(AbstractContainerScreen<?> gui) {
        var slots = gui.getMenu().slots;
        for (int i = lastCheckedSlot; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (slot == null || !slot.hasItem()) continue;
            ItemStack stack = slot.getItem();
            if (shouldDeposit || shouldDump) {
                if (slot.container != mc.player.getInventory()) continue;
                if (shouldDump || allowedItem(stack)) {
                    click(gui, slot);
                    lastCheckedSlot = i + 1;
                    return true;
                }
            } else if (shouldTake) {
                if (slot.container == mc.player.getInventory()) continue;
                if (allowedItem(stack)) {
                    click(gui, slot);
                    lastCheckedSlot = i + 1;
                    return true;
                }
            } else {
                Meowtils.addMessage(ChatFormatting.RED + "Unable to activate AutoChest.");
                return false;
            }
        }
        return false;
    }

    private void click(AbstractContainerScreen<?> gui, Slot slot) {
        if (mc.gameMode == null) return;
        mc.gameMode.handleContainerInput(gui.getMenu().containerId, slot.index, 0, ContainerInput.QUICK_MOVE, mc.player);
        if (Settings.bool(this, "renderClicked", true)) CLICKED_SLOTS.add(slot.index);
    }

    private boolean allowedItem(ItemStack stack) {
        String kind = BedwarsSupport.resourceKey(stack);
        if (kind.isEmpty()) return false;
        return AutoChestGate.allowedResource(
                kind,
                Settings.bool(this, "iron", true),
                Settings.bool(this, "gold", true),
                Settings.bool(this, "diamonds", true),
                Settings.bool(this, "emeralds", true));
    }

    private boolean allowedChest(ContainerScreen screen, ChestMenu menu) {
        String title = screen.getTitle().getString();
        boolean typedEnder = menu.getContainer() instanceof PlayerEnderChestContainer;
        Kind kind = AutoChestGate.classify(
                typedEnder,
                title,
                Server.HYPIXEL.isActive(),
                LOCAL_CHEST,
                LOCAL_ENDER_CHEST);
        return AutoChestGate.allow(
                Settings.bool(this, "enderChests", true),
                Settings.bool(this, "normalChests", false),
                kind,
                ShopHelper.isShopTitle(title));
    }

    private static void disable() {
        shouldDeposit = false;
        shouldTake = false;
        shouldDump = false;
    }

    @Override
    public void onDisable() {
        CLICKED_SLOTS.clear();
        disable();
    }
}
