package wtf.tatp.meowtils.module.skywars;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.module.bedwars.BedwarsSupport;
import wtf.tatp.meowtils.module.render.WorldOverlay;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;

public final class StrengthESP extends Module {
    @Config public boolean enabled;
    @Config public int key;
    @Config public int red = wtf.tatp.meowtils.module.meowtils.GUI.BLUE_DEFAULT;
    @Config public int green;
    @Config public int blue;
    @Config public boolean fillBox = true;
    @Config public String mode = "3D";
    @Config public boolean fadeOut = true;

    public StrengthESP() {
        super("StrengthESP", Category.Skywars);
        tag(ModuleTag.SAFE);
        tooltip("Renders a box on players who have the Strength effect.");
        addMode(new ModeValue("Mode", List.of("3D", "2D"), "mode", this));
        ColorLink color = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("ESP color", color));
        addSaturation(new SaturationValue(color));
        addBrightness(new BrightnessValue(color));
        addToggle(new ToggleValue("Fill entire box", "fillBox", this));
        addToggle(new ToggleValue("Fade out", "fadeOut", this));
    }

    @EventTarget
    public void onWorld(RenderWorldLastEvent event) {
        if (mc.level == null || mc.player == null) return;
        boolean twoD = "2D".equalsIgnoreCase(Settings.text(this, "mode", mode));
        boolean fill = Settings.bool(this, "fillBox", fillBox);
        float partial = WorldOverlay.partialTick();
        for (Player player : mc.level.players()) {
            int color = boxColor(player);
            if (color == 0) continue;
            AABB box = WorldOverlay.interpolated(player, partial).inflate(0.1, 0.1, 0.1);
            if (twoD) {
                WorldOverlay.billboard(event, box, color, fill);
            } else {
                if (fill) WorldOverlay.filledBox(event, box, color);
                WorldOverlay.outline(event, box, color, fill ? 3f : 2f);
            }
        }
    }

    private int boxColor(Player player) {
        if (player == mc.player) return 0;
        MobEffectInstance effect = strengthEffect(player);
        boolean potion = effect == null && holdingStrengthPotion(player);
        if (effect == null && !potion) return 0;
        int alpha = 255;
        if (Settings.bool(this, "fadeOut", fadeOut)) {
            if (effect != null) alpha = (int) (150f * Math.min(1f, effect.getDuration() / 100f));
            else alpha = 150;
        }
        if (alpha <= 0) return 0;
        return ColorUtil.rgba(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue), alpha);
    }

    private static MobEffectInstance strengthEffect(Player player) {
        if (player.hasEffect(MobEffects.STRENGTH)) return player.getEffect(MobEffects.STRENGTH);
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getDescriptionId().toLowerCase(java.util.Locale.ROOT).contains("strength")) return effect;
        }
        return null;
    }

    private static boolean holdingStrengthPotion(Player player) {
        ItemStack held = player.getMainHandItem();
        if (!ItemIds.is(held, "potion")) return false;
        if (textHasStrength(held.getHoverName().getString())) return true;
        var contents = held.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            for (var effect : contents.getAllEffects()) {
                if (effect.getDescriptionId().toLowerCase(java.util.Locale.ROOT).contains("strength")) return true;
            }
        }
        for (String line : BedwarsSupport.loreLines(held)) {
            if (textHasStrength(line)) return true;
        }
        return false;
    }

    private static boolean textHasStrength(String text) {
        return text != null && ColorUtil.unformattedText(text).toLowerCase(java.util.Locale.ROOT).contains("strength");
    }
}
