package wtf.tatp.meowtils.module.render;

import java.util.List;
import net.minecraft.world.item.ItemUseAnimation;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

/** Visual first-person animation overrides. Mixins apply cancel/block transforms. */
public final class Animations extends Module {
    @Config public boolean fakeAutoblock = true;
    @Config public String autoblockMode = "Always";
    @Config public boolean cancelSwing;
    @Config public boolean cancelSwingRightClick;
    @Config public boolean cancelConsume;
    @Config public boolean cancelBow;
    private static long lastClickTime;
    private static boolean killaura;

    public Animations() {
        super("Animations", Category.Render);
        addToggle(new ToggleValue("Fake autoblock", "fakeAutoblock", this));
        addMode(new ModeValue("Activate", List.of("Always", "Rightclick", "Killaura"), "autoblockMode", this));
        addToggle(new ToggleValue("Cancel swing animation", "cancelSwing", this));
        addCheck(new CheckValue("Rightclick only", "cancelSwingRightClick", this));
        addToggle(new ToggleValue("Cancel consume animation", "cancelConsume", this));
        addToggle(new ToggleValue("Cancel bow animation", "cancelBow", this));
        tag(ModuleTag.LEGIT);
        tooltip("Allows you to change certain animations. Visual only.\n§eNote: §eRightclick §eonly §eworks §eon §ewindows.");
    }

    @EventTarget
    public void onTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!"Killaura".equals(Settings.text(this, "autoblockMode", "Always"))) return;
        if (mc.options.keyAttack.isDown() || mc.options.keyUse.isDown()) lastClickTime = System.currentTimeMillis();
        long sinceClick = System.currentTimeMillis() - lastClickTime;
        killaura = mc.player.swinging && !mc.options.keyAttack.isDown() && !mc.options.keyUse.isDown() && sinceClick > 500;
    }

    public static boolean shouldBlock() {
        Animations module = get(Animations.class);
        if (module == null || !module.getState() || !Settings.bool(module, "fakeAutoblock", true) || module.mc.player == null) return false;
        if (!ItemIds.isSword(module.mc.player.getMainHandItem())) return false;
        return switch (Settings.text(module, "autoblockMode", "Always")) {
            // 1.8 ViaVersion sword-block is use-item (keyUse), not a shield. Do not call isUsingItem here — the LocalPlayer mixin ORs this result into isUsingItem.
            case "Always" -> module.mc.player.swinging || module.mc.options.keyUse.isDown();
            case "Rightclick" -> module.mc.options.keyUse.isDown();
            case "Killaura" -> killaura;
            default -> false;
        };
    }

    public static boolean cancelSwing() {
        Animations module = get(Animations.class);
        if (module == null || !module.getState() || !Settings.bool(module, "cancelSwing", false)) return false;
        if (Settings.bool(module, "cancelSwingRightClick", false)) {
            return module.mc.player != null && (module.mc.player.isUsingItem() || module.mc.options.keyUse.isDown() || shouldBlock());
        }
        return true;
    }

    public static ItemUseAnimation rewriteUseAnimation(ItemUseAnimation action) {
        Animations module = get(Animations.class);
        if (module == null || !module.getState()) return action;
        if (shouldBlock()) return ItemUseAnimation.BLOCK;
        if (Settings.bool(module, "cancelConsume", false) && (action == ItemUseAnimation.EAT || action == ItemUseAnimation.DRINK)) return ItemUseAnimation.NONE;
        if (Settings.bool(module, "cancelBow", false) && action == ItemUseAnimation.BOW) return ItemUseAnimation.NONE;
        return action;
    }

    /** Original {@code MixinEntityPlayerSP.func_71052_bv} — fake 1.8 block use-count. */
    public static int fakeUseTicks(int real) {
        return shouldBlock() ? 10 : real;
    }

    /** Original {@code MixinEntityPlayerSP.func_70632_aY} — treat fake autoblock as using-item. */
    public static boolean fakeUsing(boolean real) {
        return real || shouldBlock();
    }
}
