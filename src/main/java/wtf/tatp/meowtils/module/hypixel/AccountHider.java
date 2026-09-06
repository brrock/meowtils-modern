package wtf.tatp.meowtils.module.hypixel;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import wtf.tatp.meowtils.CommandManager;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.manager.SkinManager;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.Util;

public final class AccountHider extends Module {
    public AccountHider() {
        super("AccountHider", Category.Hypixel);
        tag(ModuleTag.LEGIT);
        tooltip("Visually modify account information.\n§d/customname <name> §f- Set custom name");
        addButton(new ButtonValue("Skin folder", 5.0f, () -> Util.openFolder(SkinManager.directory(), "skin")));
        CommandManager.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("customname")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            setCustomName(StringArgumentType.getString(context, "name"));
                            return 1;
                        }))
                .executes(context -> {
                    Meowtils.addMessage("§cUsage: /customname <text>");
                    return 0;
                }));
    }

    public static void setCustomName(String name) {
        AccountHider module = get(AccountHider.class);
        if (module == null) return;
        module.settingsStorage().put("customName", name);
        ConfigManager.save();
        Meowtils.addMessage("§aSet custom name to: §r" + name);
    }

    public static String customName() {
        AccountHider module = get(AccountHider.class);
        if (module == null) return "You";
        String name = Settings.text(module, "customName", "You");
        return name.isEmpty() ? "You" : ColorUtil.convertFormatting(name);
    }

    public static boolean hideName() {
        AccountHider module = get(AccountHider.class);
        return module != null && module.getState() && Settings.bool(module, "hideName", false);
    }

    public static boolean customSkin() {
        AccountHider module = get(AccountHider.class);
        return module != null && module.getState() && Settings.bool(module, "skin", false);
    }

    public static String skinLocation() {
        AccountHider module = get(AccountHider.class);
        return module == null ? "" : Settings.text(module, "skinLocation", "");
    }

    public static String armMode() {
        AccountHider module = get(AccountHider.class);
        return module == null ? "Default" : Settings.text(module, "armMode", "Default");
    }

    public static Identifier armTexture() {
        if (!customSkin()) return null;
        var body = SkinManager.getSkin(skinLocation());
        return body == null ? null : body.texturePath();
    }

    public static PlayerSkin applySkin(PlayerSkin original) {
        if (!customSkin()) return original;
        var body = SkinManager.getSkin(skinLocation());
        if (body == null) return original;
        PlayerModelType model = "Slim".equals(armMode()) ? PlayerModelType.SLIM : PlayerModelType.WIDE;
        if (original == null) return PlayerSkin.insecure(body, null, null, model);
        return PlayerSkin.insecure(body, original.cape(), original.elytra(), model);
    }

    public static Component renameTab(PlayerInfo info, Component current) {
        if (!hideName() || current == null) return current;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return current;
        String playerName = client.player.getGameProfile().name();
        if (playerName == null || playerName.isEmpty() || !current.getString().contains(playerName)) return current;
        if (info != null && !info.getProfile().id().equals(client.player.getUUID()) && !current.getString().contains(playerName)) {
            return current;
        }
        return rewrite(current, playerName, customName());
    }

    public static Component renameComponent(Component current) {
        if (!hideName() || current == null) return current;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return current;
        String playerName = client.player.getGameProfile().name();
        if (playerName == null || playerName.isEmpty() || !current.getString().contains(playerName)) return current;
        return rewrite(current, playerName, customName());
    }

    private static Component rewrite(Component current, String from, String to) {
        MutableComponent out = Component.empty();
        current.visit((Style style, String text) -> {
            out.append(Component.literal(text.replace(from, to)).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    public static boolean needsRename(String text) {
        if (text == null || text.isEmpty()) return false;
        AccountHider module = get(AccountHider.class);
        if (module == null || !module.getState() || !Settings.bool(module, "hideName", false)) return false;
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        String playerName = player.getGameProfile().name();
        return playerName != null && !playerName.isEmpty() && text.contains(playerName);
    }

    @EventTarget(priority = EventPriority.LOWEST)
    public void onRenderString(RenderStringEvent event) {
        if (mc.player == null || mc.level == null || event.getString() == null || !Settings.bool(this, "hideName", false)) return;
        String text = event.getString();
        String playerName = mc.player.getGameProfile().name();
        if (playerName != null && text.contains(playerName)) event.setString(text.replace(playerName, customName()));
    }
}
