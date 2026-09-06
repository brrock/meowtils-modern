package wtf.tatp.meowtils.module.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

/** Potion list with the original effect filters, infinite hide, and expire sound. */
public final class PotionHUD extends Module {
    @wtf.tatp.meowtils.config.Config public int posX = 1, posY = 80;
    @wtf.tatp.meowtils.config.Config public float scale = 0.65f;
    @wtf.tatp.meowtils.config.Config public boolean expireSound = true, hideInfinite = true;
    @wtf.tatp.meowtils.config.Config public boolean invis = true, jump = true, speed = true, regen = true, strength = true, fireRes = true, haste = true, fatigue = true;
    private static final Set<String> ALERTED = new HashSet<>();

    public PotionHUD() {
        super("PotionHUD", Category.Render);
        addMode(new ModeValue("Name", List.of("Full", "Short", "None"), "name", this));
        addMode(new ModeValue("Display", List.of("Both", "HUD", "Chat"), "display", this));
        addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, Float.TYPE));
        addSlider(new SliderValue("Alert time", 0, 10, 1, "s", "alertTime", this, Integer.TYPE));
        addToggle(new ToggleValue("Expire sound", "expireSound", this));
        addToggle(new ToggleValue("Hide infinite effects", "hideInfinite", this));
        addExpand(new ExpandValue("Potions", e -> {
            e.addCheck(new CheckValue("§bInvisibility", "invis", this));
            e.addCheck(new CheckValue("§aJump Boost", "jump", this));
            e.addCheck(new CheckValue("§eSpeed", "speed", this));
            e.addCheck(new CheckValue("§dRegeneration", "regen", this));
            e.addCheck(new CheckValue("§4Strength", "strength", this));
            e.addCheck(new CheckValue("§6Fire Resistance", "fireRes", this));
            e.addCheck(new CheckValue("§9Haste", "haste", this));
            e.addCheck(new CheckValue("§7Mining Fatigue", "fatigue", this));
        }, this));
        settingsStorage().put("alertTime", 5);
        settingsStorage().put("scale", 0.65);
        settingsStorage().put("expireSound", true);
        settingsStorage().put("hideInfinite", true);
        for (String key : List.of("invis", "jump", "speed", "regen", "strength", "fireRes", "haste", "fatigue")) {
            settingsStorage().put(key, true);
        }
        tooltip("Display important potion effects on screen.");
        tag(ModuleTag.LEGIT);
    }

    @Override
    public List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        return List.of(new wtf.tatp.meowtils.gui.hudeditor.HudEntry(null, this, "posX", "posY",
                () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("Fire Resistance: 10_00", 8, (float) Settings.number(this, "scale", scale))));
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean preview = wtf.tatp.meowtils.gui.GuiUtil.inEditor();
        if (!preview && mc.gui.screen() != null) return;
        String nameMode = Settings.text(this, "name", "Full");
        String displayMode = Settings.text(this, "display", "Both");
        int alertTicks = Settings.integer(this, "alertTime", 5) * 20;
        List<String> lines = new ArrayList<>();
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            NamedPotion named = named(effect.getEffect());
            if (named == null || !Settings.bool(this, named.config, true)) continue;
            boolean infinite = effect.isInfiniteDuration() || effect.getDuration() > 36000;
            if (Settings.bool(this, "hideInfinite", true) && infinite) continue;
            String timeLeft = infinite ? "**:**" : formatTime(Math.max(0, effect.getDuration()));
            if (!infinite && effect.getDuration() <= alertTicks) {
                timeLeft = !"None".equals(nameMode) ? ChatFormatting.RED + timeLeft : timeLeft;
                if (ALERTED.add(named.config) && ("Chat".equals(displayMode) || "Both".equals(displayMode))) {
                    String suffix = alertTicks <= 0 ? " has expired!" : " is about to expire!";
                    Meowtils.addMessage(named.full.replace(":", "") + ChatFormatting.RED + suffix);
                    if (Settings.bool(this, "expireSound", true)) Util.playSound(Util.Sound.PING_MEDIUM, 100);
                }
            } else {
                ALERTED.remove(named.config);
            }
            lines.add(named.label(nameMode) + " " + ("None".equals(nameMode) ? "" : ChatFormatting.GRAY) + timeLeft);
        }
        lines.sort(Comparator.comparingDouble((String line) -> wtf.tatp.meowtils.font.HudFont.width(line, 1)).reversed());
        if (preview && lines.isEmpty()) {
            lines.add(ChatFormatting.YELLOW + "Speed: " + ChatFormatting.GRAY + "1:30");
            lines.add(ChatFormatting.DARK_RED + "Strength: " + ChatFormatting.GRAY + "0:15");
        }
        if (lines.isEmpty() || ("Chat".equals(displayMode) && !preview)) return;
        float drawScale = (float) Settings.number(this, "scale", scale);
        int y = posY;
        for (String line : lines) {
            wtf.tatp.meowtils.font.HudFont.draw(event.getGraphics(), line, posX, y, drawScale, 0xFFFFFFFF);
            y += wtf.tatp.meowtils.font.HudFont.lineOffset(drawScale);
        }
    }

    private static String formatTime(int ticks) {
        int seconds = ticks / 20;
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

    private static NamedPotion named(Holder<MobEffect> effect) {
        if (effect.is(MobEffects.INVISIBILITY)) return new NamedPotion("invis", "Invisibility", "Inv", ChatFormatting.AQUA);
        if (effect.is(MobEffects.JUMP_BOOST)) return new NamedPotion("jump", "Jump Boost", "Jmp", ChatFormatting.GREEN);
        if (effect.is(MobEffects.SPEED)) return new NamedPotion("speed", "Speed", "Spd", ChatFormatting.YELLOW);
        if (effect.is(MobEffects.REGENERATION)) return new NamedPotion("regen", "Regeneration", "Reg", ChatFormatting.LIGHT_PURPLE);
        if (effect.is(MobEffects.STRENGTH)) return new NamedPotion("strength", "Strength", "Str", ChatFormatting.DARK_RED);
        if (effect.is(MobEffects.FIRE_RESISTANCE)) return new NamedPotion("fireRes", "Fire Resistance", "Fire", ChatFormatting.GOLD);
        if (effect.is(MobEffects.MINING_FATIGUE)) return new NamedPotion("fatigue", "Mining Fatigue", "Mine", ChatFormatting.GRAY);
        if (effect.is(MobEffects.HASTE)) return new NamedPotion("haste", "Haste", "Hst", ChatFormatting.BLUE);
        return null;
    }

    private record NamedPotion(String config, String full, String shortName, ChatFormatting color) {
        String label(String mode) {
            return switch (mode) {
                case "Short" -> color + shortName + ":";
                case "None" -> color.toString();
                default -> color + full + ":";
            };
        }
    }
}
