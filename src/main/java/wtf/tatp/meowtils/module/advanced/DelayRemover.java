package wtf.tatp.meowtils.module.advanced;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.mixin.DelayRemoverGameModeAccessor;
import wtf.tatp.meowtils.mixin.KeyMappingAccessor;
import wtf.tatp.meowtils.mixin.LivingEntityAccessor;
import wtf.tatp.meowtils.util.Settings;

/** Client-side reduction of break, use, miss, and jump delays. */
public final class DelayRemover extends Module {
    private static int useTicks;

    public DelayRemover() {
        super("DelayRemover", Category.Advanced);
        tag(ModuleTag.BLATANT);
        tooltip("Remove or reduce certain delays.\n§6NoBreakDelay §f- Remove or reduce delay between block breaks\n§6NoUseDelay §f- Releases right click after required use duration\n§6NoHitDelay §f- Removes delay if you miss an attack\n§6NoJumpDelay §f- Removes delay between jumps\n§cWARNING: §cSome §cof §cthese §cfeatures §cmay §cbe §cdetectable.");
        addExpand(new ExpandValue("NoBreakDelay", e -> {
            e.addToggle(new ToggleValue("Enabled", "noBreakDelay", this));
            e.addSlider(new SliderValue("Delay", 0, 5, 1, "ticks", "breakDelay", this, Integer.class));
        }, this));
        addExpand(new ExpandValue("NoUseDelay", e -> {
            e.addToggle(new ToggleValue("Enabled", "noUseDelay", this));
            e.addSlider(new SliderValue("Delay", 0, 4, 1, "ticks", "useDelay", this, Integer.class));
        }, this));
        addExpand(new ExpandValue("NoHitDelay", e -> e.addToggle(new ToggleValue("Enabled", "noHitDelay", this)), this));
        addExpand(new ExpandValue("NoJumpDelay", e -> {
            e.addToggle(new ToggleValue("Enabled", "noJumpDelay", this));
            e.addSlider(new SliderValue("Delay", 0, 10, 1, "ticks", "jumpDelay", this, Integer.class));
        }, this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (mc.gameMode != null && Settings.bool(this, "noBreakDelay", true)) {
            DelayRemoverGameModeAccessor accessor = (DelayRemoverGameModeAccessor) mc.gameMode;
            int delay = Settings.integer(this, "breakDelay", 0);
            if (accessor.meowtils$getDestroyDelay() > delay) accessor.meowtils$setDestroyDelay(delay);
        }
        if (Settings.bool(this, "noUseDelay", true)) {
            ItemStack held = mc.player.getMainHandItem();
            boolean validItem = !held.isEmpty() && (held.has(DataComponents.FOOD) || held.has(DataComponents.POTION_CONTENTS) || held.getItem() instanceof PotionItem);
            boolean holdingUse = mc.options.keyUse.isDown();
            if (holdingUse && validItem) {
                useTicks++;
                if (useTicks >= 32 + Settings.integer(this, "useDelay", 1)) {
                    net.minecraft.client.KeyMapping.set(((KeyMappingAccessor) (Object) mc.options.keyUse).meowtils$getKey(), false);
                    useTicks = 0;
                }
            } else {
                useTicks = 0;
            }
        }
        if (Settings.bool(this, "noHitDelay", true)) mc.missTime = 0;
        if (Settings.bool(this, "noJumpDelay", true)) {
            LivingEntityAccessor accessor = (LivingEntityAccessor) (Object) mc.player;
            accessor.meowtils$setNoJumpDelay(Math.min(accessor.meowtils$getNoJumpDelay(), Settings.integer(this, "jumpDelay", 0)));
        }
    }
}
