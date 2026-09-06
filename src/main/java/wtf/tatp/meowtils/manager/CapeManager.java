package wtf.tatp.meowtils.manager;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.render.Cape;
import wtf.tatp.meowtils.util.Settings;

/** Bundled and custom cape textures for the Cape module. */
public final class CapeManager {
    private static final int FRAME_DELAY = 100;
    private static String lastCape;
    private static ClientAsset.Texture cachedCape;
    private static ClientAsset.Texture[] cachedFrames;

    private CapeManager() {}

    public static File directory() {
        return MeowtilsData.customCape().toFile();
    }

    /** Prefer original `custom_cape`, then the older port `capes` folder. */
    private static File resolveCustomCape(String name, String extension) {
        File preferred = new File(directory(), name + extension);
        if (preferred.isFile()) return preferred;
        File legacy = MeowtilsData.legacyCapes().resolve(name + extension).toFile();
        return legacy.isFile() ? legacy : preferred;
    }

    public static ClientAsset.Texture textureFor(Entity entity) {
        Cape module = Module.get(Cape.class);
        if (module == null || !module.getState() || !(entity instanceof Player player)) return null;
        boolean self = player == Minecraft.getInstance().player;
        if (!self && !Settings.bool(module, "renderOnAll", false)) return null;
        return getCape(Settings.text(module, "selectedCape", "2011"));
    }

    public static ClientAsset.Texture getCape(String selectedCape) {
        Cape module = Module.get(Cape.class);
        String custom = module == null ? "" : Settings.text(module, "customCapeName", "");
        String capeKey = "Custom".equalsIgnoreCase(selectedCape) ? "custom_" + custom : selectedCape;
        if (!capeKey.equals(lastCape)) {
            lastCape = capeKey;
            cachedCape = null;
            cachedFrames = null;
            cachedCape = loadCape(selectedCape);
        }
        if (cachedFrames != null && cachedFrames.length > 0) {
            int frame = (int) ((System.currentTimeMillis() / FRAME_DELAY) % cachedFrames.length);
            return cachedFrames[frame];
        }
        return cachedCape;
    }

    private static ClientAsset.Texture loadCape(String id) {
        try {
            if ("Custom".equalsIgnoreCase(id)) {
                Cape module = Module.get(Cape.class);
                String name = module == null ? "" : Settings.text(module, "customCapeName", "");
                name = name.replace(".png", "").replace(".gif", "");
                if (name.isBlank()) return null;
                File gif = resolveCustomCape(name, ".gif");
                File png = resolveCustomCape(name, ".png");
                File file = gif.isFile() ? gif : png;
                if (!file.isFile()) return null;
                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".gif")) {
                    try (InputStream in = java.nio.file.Files.newInputStream(file.toPath())) {
                        return loadGif(in, "custom_" + name);
                    }
                }
                return registerFile("custom_" + name, NativeImage.read(java.nio.file.Files.newInputStream(file.toPath())));
            }
            String key = id.toLowerCase(Locale.ROOT);
            Identifier bundled = Identifier.fromNamespaceAndPath("meowtils", "capes/" + key);
            if (Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath("meowtils", "textures/capes/" + key + ".png")).isPresent()) {
                return new ClientAsset.ResourceTexture(bundled);
            }
            try (InputStream in = CapeManager.class.getClassLoader().getResourceAsStream("assets/meowtils/textures/capes/" + key + ".png")) {
                if (in == null) return new ClientAsset.ResourceTexture(bundled);
                return registerFile(key, NativeImage.read(in));
            }
        } catch (Exception e) {
            Meowtils.error("Failed to load cape: " + e);
            return null;
        }
    }

    private static ClientAsset.Texture loadGif(InputStream inputStream, String key) throws Exception {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) return null;
        ImageReader reader = readers.next();
        List<ClientAsset.Texture> frames = new ArrayList<>();
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(inputStream)) {
            reader.setInput(imageInput, false);
            int count = reader.getNumImages(true);
            BufferedImage canvas = null;
            for (int i = 0; i < count; i++) {
                BufferedImage frame = reader.read(i);
                if (frame == null) continue;
                if (canvas == null) canvas = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                int left = 0;
                int top = 0;
                IIOMetadata metadata = reader.getImageMetadata(i);
                Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
                for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                    if (!"ImageDescriptor".equals(node.getNodeName())) continue;
                    NamedNodeMap attributes = node.getAttributes();
                    left = Integer.parseInt(attributes.getNamedItem("imageLeftPosition").getNodeValue());
                    top = Integer.parseInt(attributes.getNamedItem("imageTopPosition").getNodeValue());
                    break;
                }
                Graphics2D graphics = canvas.createGraphics();
                graphics.drawImage(frame, left, top, null);
                graphics.dispose();
                frames.add(registerImage(key, i, canvas));
            }
        } finally {
            reader.dispose();
        }
        if (frames.isEmpty()) return null;
        cachedFrames = frames.toArray(ClientAsset.Texture[]::new);
        return cachedFrames[0];
    }

    private static ClientAsset.Texture registerImage(String key, int frame, BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) nativeImage.setPixel(x, y, image.getRGB(x, y));
        }
        return registerFile(key + "_" + frame, nativeImage);
    }

    private static ClientAsset.Texture registerFile(String key, NativeImage image) {
        Identifier identifier = Identifier.fromNamespaceAndPath("meowtils", "cape/" + sanitize(key));
        Minecraft.getInstance().getTextureManager().register(identifier, new DynamicTexture(() -> "meowtils_cape/" + key, image));
        return new ClientAsset.ResourceTexture(identifier, identifier);
    }

    private static String sanitize(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (char c : name.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '/') out.append(c);
            else out.append('_');
        }
        return out.isEmpty() ? "custom" : out.toString();
    }
}
