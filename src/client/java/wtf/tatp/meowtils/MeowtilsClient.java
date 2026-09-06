package wtf.tatp.meowtils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.lwjgl.glfw.GLFW;
import wtf.tatp.meowtils.extension.ExtensionManager;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.module.RegisterModule;
import wtf.tatp.meowtils.command.RegisterCommand;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.gui.ClickGuiScreen;
import wtf.tatp.meowtils.manager.NotificationManager;

public final class MeowtilsClient implements ClientModInitializer {
    public static final String MOD_ID = "meowtils";
    private static Minecraft client;
    private static KeyMapping openGuiKey;
    private static int lastGuiKey=GLFW.GLFW_KEY_RIGHT_SHIFT;

    @Override
    public void onInitializeClient() {
        client = Minecraft.getInstance();
        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.meowtils.open_gui",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keybinds"))));
        wtf.tatp.meowtils.MeowtilsData.ensure();
        wtf.tatp.meowtils.manager.log.LogManager.init();
        RegisterModule.registerAll();
        long refused = wtf.tatp.meowtils.gui.ModuleManager.getModules().stream()
                .filter(module -> !module.isBehaviorAvailable()).count();
        wtf.tatp.meowtils.Meowtils.info("Registered " + wtf.tatp.meowtils.gui.ModuleManager.getModules().size()
                + " modules; " + refused + " still refuse activation.");
        wtf.tatp.meowtils.manager.SoundLoader.init();
        EventManager.register(new wtf.tatp.meowtils.MeowtilsAlert());
        EventManager.register(new NotificationManager());
        EventManager.register(new wtf.tatp.meowtils.manager.session.SessionManager());
        EventManager.register(new wtf.tatp.meowtils.util.TeamUtil());
        EventManager.register(new wtf.tatp.meowtils.handler.PartyHandler());
        EventManager.register(new wtf.tatp.meowtils.handler.ResetHandler());
        EventManager.register(new wtf.tatp.meowtils.handler.LatencyHandler());
        EventManager.register(new wtf.tatp.meowtils.manager.updater.UpdateManager());
        wtf.tatp.meowtils.manager.icons.RegisterIcon.init();
        wtf.tatp.meowtils.module.hypixel.NickBot.init();
        ExtensionManager.initialize();
        ConfigManager.initialize(net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir());
        lastGuiKey=Module.get(wtf.tatp.meowtils.module.meowtils.GUI.class).getKey();
        openGuiKey.setKey(InputConstants.Type.KEYSYM.getOrCreate(lastGuiKey==0?GLFW.GLFW_KEY_UNKNOWN:lastGuiKey));
        KeyMapping.resetMapping();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, joinedClient) -> {
            if (joinedClient.level != null) EventManager.post(new WorldEvent(joinedClient.level, WorldEvent.Type.LOAD));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, disconnectedClient) -> {
            if (disconnectedClient.level != null) EventManager.post(new WorldEvent(disconnectedClient.level, WorldEvent.Type.UNLOAD));
            ConfigManager.save();
        });
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(stopping -> {
            wtf.tatp.meowtils.MeowtilsData.ensure();
            ConfigManager.save();
            ExtensionManager.shutdown();
        });
        ClientTickEvents.END_CLIENT_TICK.register(ignored -> {
            EventManager.post(new ClientTickEvent(client, ClientTickEvent.Phase.PRE));
            EventManager.post(new wtf.tatp.meowtils.event.RenderTickEvent(client, wtf.tatp.meowtils.event.RenderTickEvent.Phase.PRE));
            EventManager.post(new ClientTickEvent(client));
            int guiKey=Module.get(wtf.tatp.meowtils.module.meowtils.GUI.class).getKey();
            if (guiKey!=lastGuiKey) {
                lastGuiKey=guiKey;
                openGuiKey.setKey(InputConstants.Type.KEYSYM.getOrCreate(guiKey==0?GLFW.GLFW_KEY_UNKNOWN:guiKey));
                KeyMapping.resetMapping();
            }
            while (openGuiKey.consumeClick()) if (client.gui.screen()==null) client.setScreenAndShow(new ClickGuiScreen());
            KeybindManager.tick();
            ExtensionManager.tick();
            EventManager.post(new wtf.tatp.meowtils.event.RenderTickEvent(client, wtf.tatp.meowtils.event.RenderTickEvent.Phase.POST));
            EventManager.post(new ClientTickEvent(client, ClientTickEvent.Phase.POST));
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (Meowtils.isPostingLocalChat()) return true;
            wtf.tatp.meowtils.event.ChatReceivedEvent event = new wtf.tatp.meowtils.event.ChatReceivedEvent(message, overlay);
            EventManager.post(event);
            return !event.isCancelled();
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "meowtils_events"),
                (graphics, delta) -> EventManager.post(new wtf.tatp.meowtils.event.RenderGameOverlayEvent(graphics, delta)));
        LevelRenderEvents.END_MAIN.register(context ->
                EventManager.post(new wtf.tatp.meowtils.event.RenderWorldLastEvent(context)));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            wtf.tatp.meowtils.event.AttackEntityEvent event = new wtf.tatp.meowtils.event.AttackEntityEvent(entity);
            EventManager.post(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClientSide() || !(player instanceof net.minecraft.client.player.LocalPlayer local) || !(world instanceof net.minecraft.client.multiplayer.ClientLevel level)) return InteractionResult.PASS;
            var event = new wtf.tatp.meowtils.event.PlayerInteractEvent(local, level, wtf.tatp.meowtils.event.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, hit.getBlockPos(), hit.getDirection());
            EventManager.post(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide() || !(player instanceof net.minecraft.client.player.LocalPlayer local) || !(world instanceof net.minecraft.client.multiplayer.ClientLevel level)) return InteractionResult.PASS;
            var event = new wtf.tatp.meowtils.event.PlayerInteractEvent(local, level, wtf.tatp.meowtils.event.PlayerInteractEvent.Action.RIGHT_CLICK_AIR, local.blockPosition(), local.getDirection());
            EventManager.post(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClientSide() || !(player instanceof net.minecraft.client.player.LocalPlayer local) || !(world instanceof net.minecraft.client.multiplayer.ClientLevel level)) return InteractionResult.PASS;
            var event = new wtf.tatp.meowtils.event.PlayerInteractEvent(local, level, wtf.tatp.meowtils.event.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, pos, direction);
            EventManager.post(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            CommandManager.install(dispatcher);
            RegisterCommand.init();
        });
    }

    public static Minecraft client() {
        return client;
    }

    public static void openClickGui() {
        if (client != null) client.setScreenAndShow(new ClickGuiScreen());
    }
}
