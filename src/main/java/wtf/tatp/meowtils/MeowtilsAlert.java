package wtf.tatp.meowtils;

import java.net.URI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.log.LogManager;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.Prefix;

/** One-shot welcome / docs reminder after 60 in-world POST ticks, matching 2.0.1. */
public final class MeowtilsAlert {
    private static int tickCounter;

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        Minecraft mc = event.getClient() != null ? event.getClient() : Minecraft.getInstance();
        GUI gui = Module.get(GUI.class);
        if (mc.player == null || mc.level == null || event.getPhase() != ClientTickEvent.Phase.POST || gui == null) {
            return;
        }
        tickCounter++;
        if (tickCounter < 60) return;

        Notifications notifications = Module.get(Notifications.class);
        boolean startNotifications = notifications != null && notifications.startNotifications;
        if (startNotifications && !gui.firstStartup) {
            documentationMessage();
        }
        if (gui.firstStartup) {
            Meowtils.addMessage(ChatFormatting.BLUE + "Welcome to " + ChatFormatting.DARK_PURPLE.toString()
                    + ChatFormatting.BOLD + "Meowtils" + ChatFormatting.BLUE + "!");
            Meowtils.addMessage(ChatFormatting.RED + "Open GUI: " + ChatFormatting.GREEN + "Right Shift");
            Meowtils.addMessage(ChatFormatting.GRAY + "If you are unable to open the GUI, you can use the "
                    + ChatFormatting.GREEN + "/meowtilsgui" + ChatFormatting.GRAY + " command and rebind in the "
                    + ChatFormatting.BLUE + "GUI Module" + ChatFormatting.GRAY + " or use the "
                    + ChatFormatting.GREEN + "/bind <key>" + ChatFormatting.GRAY + " command to set it directly.");
            documentationMessage();
            gui.firstStartup = false;
            ConfigManager.save();
            LogManager.write("INFO", "First-startup welcome shown");
        }
        EventManager.unregister(this);
    }

    private static void documentationMessage() {
        var message = Component.literal(Prefix.getPrefix() + "Read the full documentation at "
                + ChatFormatting.LIGHT_PURPLE + ChatFormatting.UNDERLINE + "docs.tatp.wtf"
                + ChatFormatting.WHITE + ".");
        message.setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://docs.tatp.wtf/")))
                .withUnderlined(true));
        Meowtils.addChat(message);
    }
}
