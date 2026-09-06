package wtf.tatp.meowtils.module.render;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.HudRenderEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Settings;

/** Own health plus the original bow-damage HUD (charge/crit and Hypixel HP chat). */
public final class HealthDisplay extends Module {
    private static final Pattern BOW_HP = Pattern.compile("is on ([0-9]+(?:\\.[0-9]+)?) HP!");
    @wtf.tatp.meowtils.config.Config public int healthPosX = 310, healthPosY = 190, bowPosX = 310, bowPosY = 50;
    @wtf.tatp.meowtils.config.Config public float healthScale = 0.65f, bowScale = 1;
    @wtf.tatp.meowtils.config.Config public boolean showOwn = true, showBow = true, hideSuffix;
    private static float indicatorHealth;
    private static long lastIndicator;

    public HealthDisplay() {
        super("HealthDisplay", Category.Render);
        addExpand(new ExpandValue("Show own health", e -> {
            e.addToggle(new ToggleValue("Enabled", "showOwn", this));
            e.addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "healthScale", this, Float.TYPE));
        }, this));
        addExpand(new ExpandValue("Show bow damage", e -> {
            e.addToggle(new ToggleValue("Enabled", "showBow", this));
            e.addSlider(new SliderValue("Scale", 0.5, 5.0, 0.05, null, "bowScale", this, Float.TYPE));
            e.addCheck(new CheckValue("Hide suffix", "hideSuffix", this));
        }, this));
        settingsStorage().put("showOwn", true);
        settingsStorage().put("showBow", true);
        settingsStorage().put("healthScale", 0.65);
        settingsStorage().put("bowScale", 1);
        tooltip("Displays various health information.");
        tag(ModuleTag.LEGIT);
    }

    @Override
    public java.util.List<wtf.tatp.meowtils.gui.hudeditor.HudEntry> hudEditor() {
        var entries = new java.util.ArrayList<wtf.tatp.meowtils.gui.hudeditor.HudEntry>();
        if (Settings.bool(this, "showOwn", true)) {
            entries.add(new wtf.tatp.meowtils.gui.hudeditor.HudEntry("Health", this, "healthPosX", "healthPosY",
                    () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds("20❤", 1, (float) Settings.number(this, "healthScale", healthScale))));
        }
        if (Settings.bool(this, "showBow", true)) {
            entries.add(new wtf.tatp.meowtils.gui.hudeditor.HudEntry("Bow Damage", this, "bowPosX", "bowPosY",
                    () -> wtf.tatp.meowtils.gui.GuiUtil.getHudBounds(Settings.bool(this, "hideSuffix", false) ? "20 " : "20 HP ", 1,
                            (float) Settings.number(this, "bowScale", bowScale))));
        }
        return entries;
    }

    @EventTarget
    public void onHud(HudRenderEvent event) {
        if (mc.player == null || mc.level == null) return;
        boolean preview = wtf.tatp.meowtils.gui.GuiUtil.inEditor();
        if (!preview && mc.gui.screen() != null) return;
        var graphics = event.getGraphics();
        if (Settings.bool(this, "showOwn", true)) {
            int hearts = (int) mc.player.getHealth();
            int absorption = (int) mc.player.getAbsorptionAmount();
            boolean absorbing = mc.player.hasEffect(MobEffects.ABSORPTION);
            int health = absorbing ? hearts + absorption : hearts;
            String heart = health < 4 ? "❣" : "❤";
            ChatFormatting healthColor = healthColor(health);
            ChatFormatting heartColor = absorbing ? ChatFormatting.GOLD : ChatFormatting.RED;
            wtf.tatp.meowtils.font.HudFont.draw(graphics, healthColor.toString() + health + heartColor + heart,
                    healthPosX, healthPosY, (float) Settings.number(this, "healthScale", healthScale), 0xFFFFFFFF);
        }
        if (!Settings.bool(this, "showBow", true)) return;
        Float live = liveBowDamage();
        float shown = indicatorHealth;
        boolean visible = preview || live != null || System.currentTimeMillis() - lastIndicator <= 2000;
        if (!visible) return;
        if (live != null) shown = live;
        else if (preview && System.currentTimeMillis() - lastIndicator > 2000) shown = 8.0f;
        String suffix = Settings.bool(this, "hideSuffix", false) ? "" : " HP";
        wtf.tatp.meowtils.font.HudFont.draw(graphics, healthColor(shown) + formatDamage(shown) + suffix,
                bowPosX, bowPosY, (float) Settings.number(this, "bowScale", bowScale), 0xFFFFFFFF);
    }

    @EventTarget
    public void onChat(ChatReceivedEvent event) {
        if (event.isOverlay() || !Settings.bool(this, "showBow", true)) return;
        String msg = event.getText();
        Matcher matcher = BOW_HP.matcher(msg);
        if (matcher.find() && !msg.contains(":")) {
            indicatorHealth = Float.parseFloat(matcher.group(1));
            lastIndicator = System.currentTimeMillis();
        }
    }

    private Float liveBowDamage() {
        if (mc.player == null || !mc.player.isUsingItem()) return null;
        ItemStack stack = mc.player.getUseItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof BowItem)) return null;
        // ViaVersion 1.8 servers do not sync use-remaining; charge from client use ticks.
        return bowDamage(mc.player.getTicksUsingItem(), stack);
    }

    private static float bowDamage(int useTicks, ItemStack stack) {
        float velocity = BowItem.getPowerForTime(Math.max(0, useTicks));
        double damageField = velocity * 2.0;
        int power = enchant(stack, "power");
        if (power > 0) damageField += power * 0.5 + 0.5;
        float displayed = (float) Math.ceil(velocity * 3.0 * damageField - 1.0e-6);
        if (velocity >= 1.0f) {
            int amount = Math.max(0, (int) displayed);
            displayed += (amount / 2 + 2) / 2.0f;
        }
        return displayed;
    }

    private static int enchant(ItemStack stack, String path) {
        ItemEnchantments enchants = stack.getEnchantments();
        if (enchants == null || enchants.isEmpty()) return 0;
        int level = 0;
        for (var entry : enchants.entrySet()) {
            String name = entry.getKey().unwrapKey().map(key -> key.identifier().getPath()).orElseGet(entry.getKey()::getRegisteredName);
            if (name.toLowerCase(java.util.Locale.ROOT).contains(path)) level = Math.max(level, entry.getIntValue());
        }
        return level;
    }

    private static ChatFormatting healthColor(float health) {
        if (health < 3.0f) return ChatFormatting.DARK_RED;
        if (health < 6.0f) return ChatFormatting.RED;
        if (health < 10.0f) return ChatFormatting.YELLOW;
        if (health < 15.0f) return ChatFormatting.GREEN;
        return ChatFormatting.DARK_GREEN;
    }

    private static String formatDamage(float health) {
        return health == (int) health ? Integer.toString((int) health) : String.valueOf(health);
    }
}
