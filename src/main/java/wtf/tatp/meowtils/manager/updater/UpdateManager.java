package wtf.tatp.meowtils.manager.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.HttpJson;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.meowtils.Settings;
import wtf.tatp.meowtils.util.Prefix;

/** Checks GitHub releases once after join, matching original 2.0.1 UpdateManager. */
public final class UpdateManager {
    private static final Map<String, String> HEADERS = Map.of("User-Agent", "Meowtils-Updater");
    private static boolean verified;

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (verified || client.player == null || client.level == null || event.getPhase() != ClientTickEvent.Phase.POST) {
            return;
        }
        Meowtils.addMessage(ChatFormatting.GRAY + "Verifying " + ChatFormatting.DARK_PURPLE.toString()
                + ChatFormatting.BOLD + "Meowtils" + ChatFormatting.GRAY + " version...");
        Meowtils.warn("Verifying version.");
        verified = true;
        Thread thread = new Thread(UpdateManager::checkForUpdate, "Meowtils-Updater");
        thread.setDaemon(true);
        thread.start();
        EventManager.unregister(this);
    }

    private static void checkForUpdate() {
        try {
            String repo = Meowtils.githubRepo();
            JsonObject release = HttpJson.get(URI.create("https://api.github.com/repos/" + repo + "/releases/latest"), HEADERS).join();
            if (release == null || !release.has("tag_name")) {
                chat(ChatFormatting.RED + "Unable to fetch update.");
                Meowtils.error("Unable to fetch update.");
                return;
            }
            String latestTag = release.get("tag_name").getAsString();
            String installed = Meowtils.version();
            if (UpdateVersion.compare(installed, latestTag) >= 0) {
                chat(ChatFormatting.GREEN + "Already using latest " + ChatFormatting.DARK_PURPLE.toString()
                        + ChatFormatting.BOLD + "Meowtils" + ChatFormatting.GREEN + " version: "
                        + ChatFormatting.GRAY + installed);
                Meowtils.warn("Using latest version already: " + latestTag);
                return;
            }
            JsonArray assets = release.has("assets") ? release.getAsJsonArray("assets") : new JsonArray();
            String url = null;
            String name = null;
            for (var element : assets) {
                JsonObject asset = element.getAsJsonObject();
                String assetName = asset.get("name").getAsString();
                if (!UpdateVersion.isModJar(assetName)) continue;
                url = asset.get("browser_download_url").getAsString();
                name = assetName;
                break;
            }
            if (url == null) {
                chat(ChatFormatting.RED + "Failed to find update.");
                Meowtils.error("Failed to find update, browser_download_url was null.");
                return;
            }
            chat(ChatFormatting.DARK_PURPLE.toString() + ChatFormatting.BOLD + "Meowtils" + ChatFormatting.GREEN
                    + " update " + ChatFormatting.GRAY + latestTag + ChatFormatting.GREEN + " is available!");
            Settings settings = Module.get(Settings.class);
            if (settings != null && wtf.tatp.meowtils.util.Settings.bool(settings, "autoUpdate", true)) {
                chat(ChatFormatting.GREEN + "Downloading update...");
                download(url, name, repo);
            } else {
                String downloadUrl = url;
                Minecraft.getInstance().execute(() -> {
                    var message = Component.literal(Prefix.getPrefix() + ChatFormatting.GREEN.toString()
                            + ChatFormatting.BOLD + "Click to download update!");
                    message.setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create(downloadUrl)))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal(ChatFormatting.YELLOW + downloadUrl)))
                            .withUnderlined(true));
                    Meowtils.addChat(message);
                });
                Meowtils.warn("Auto updates are disabled, manual download is required.");
            }
        } catch (Exception exception) {
            chat(ChatFormatting.RED + "Failed to verify update.");
            Meowtils.error("Failed to verify update.");
            exception.printStackTrace();
        }
    }

    private static void download(String url, String name, String repo) {
        try {
            Path folder = MeowtilsData.autoUpdate();
            Files.createDirectories(folder);
            File oldJar = currentJar();
            String newName = name;
            if (oldJar != null) {
                String oldName = oldJar.getName();
                boolean versioned = oldName.contains("-") && oldName.substring(0, oldName.lastIndexOf('.')).contains(".");
                if (!versioned) {
                    newName = oldName;
                    chat(ChatFormatting.GREEN + "Renamed jar to: " + ChatFormatting.WHITE + oldName.replace(".jar", ""));
                }
            }
            Path target = folder.resolve(newName);
            HttpJson.download(URI.create(url), target, HEADERS).join();
            if (!registerSwap(target.toFile(), repo)) return;
            chat(ChatFormatting.GREEN + "Done!" + ChatFormatting.GRAY.toString() + ChatFormatting.ITALIC
                    + " Update will be applied next launch.");
            Meowtils.warn("Update is completed and will be applied next launch.");
        } catch (Exception exception) {
            chat(ChatFormatting.RED + "Failed to download update.");
            Meowtils.error("Failed to download update.");
            exception.printStackTrace();
        }
    }

    private static boolean registerSwap(File newJar, String repo) {
        File updater = MeowtilsData.autoUpdate().resolve("MeowtilsAutoUpdate.jar").toFile();
        if (!updater.exists()) {
            try {
                HttpJson.download(URI.create("https://github.com/" + repo + "/releases/latest/download/MeowtilsAutoUpdate.jar"),
                        updater.toPath(), HEADERS).join();
            } catch (Exception exception) {
                String page = "https://github.com/" + repo + "/releases/latest";
                Minecraft.getInstance().execute(() -> {
                    var message = Component.literal(Prefix.getPrefix() + ChatFormatting.RED
                            + "Failed to download update jar, you will have to download this manually "
                            + ChatFormatting.AQUA.toString() + ChatFormatting.BOLD + "HERE" + ChatFormatting.RED + ".");
                    message.setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create(page)))
                            .withUnderlined(true));
                    Meowtils.addChat(message);
                });
                Meowtils.error("Failed to download updater jar, manual download is required.");
                exception.printStackTrace();
                return false;
            }
        }
        File oldJar = currentJar();
        if (oldJar == null) {
            chat(ChatFormatting.RED + "Unable to find path, you will have to update manually.");
            Meowtils.error("Old jar was null and you will have to update manually.");
            return false;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                new ProcessBuilder(javaBin, "-jar", updater.getAbsolutePath(), oldJar.getAbsolutePath(), newJar.getAbsolutePath()).start();
                Meowtils.warn("Successfully registered shutdown hook for update.");
            } catch (Exception exception) {
                Meowtils.error("Unable to register shutdown hook for update jar.");
                exception.printStackTrace();
            }
        }, "Meowtils-AutoUpdate"));
        return true;
    }

    private static File currentJar() {
        try {
            var url = UpdateManager.class.getProtectionDomain().getCodeSource().getLocation();
            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                String path = uri.getSchemeSpecificPart();
                uri = new URI(path.substring(0, path.lastIndexOf('!')));
            }
            File jar = new File(uri);
            if (!jar.exists() || !jar.getName().endsWith(".jar")) {
                chat(ChatFormatting.RED + "Unexpected path, you will have to update manually.");
                Meowtils.error("Unexpected jar path, you will have to update manually. " + jar);
                return null;
            }
            return jar;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static void chat(String message) {
        Minecraft.getInstance().execute(() -> Meowtils.addMessage(message));
    }
}
