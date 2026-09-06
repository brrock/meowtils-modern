package wtf.tatp.meowtils.manager;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

/** Loads AccountHider PNGs from meowtils/custom_skins as dynamic textures. */
public final class SkinManager {
    private static String lastSkin;
    private static ClientAsset.Texture cachedTexture;

    private SkinManager() {}

    public static File directory() {
        return new File(Minecraft.getInstance().gameDirectory, "meowtils/custom_skins");
    }

    public static ClientAsset.Texture getSkin(String selectedSkin) {
        if (selectedSkin == null || selectedSkin.isBlank()) return null;
        if (!selectedSkin.equalsIgnoreCase(lastSkin)) {
            lastSkin = selectedSkin;
            cachedTexture = loadSkin(selectedSkin);
        }
        return cachedTexture;
    }

    private static ClientAsset.Texture loadSkin(String id) {
        try {
            String name = id.replace(".png", "");
            File file = new File(directory(), name + ".png");
            if (!file.isFile()) return null;
            NativeImage image;
            try (FileInputStream input = new FileInputStream(file)) {
                image = NativeImage.read(input);
            }
            Identifier identifier = Identifier.fromNamespaceAndPath("meowtils", "skin/" + sanitize(name));
            Minecraft.getInstance().getTextureManager().register(identifier, new DynamicTexture(() -> "meowtils_skin/" + name, image));
            return new ClientAsset.ResourceTexture(identifier);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String sanitize(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (char c : name.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '/') out.append(c);
            else out.append('_');
        }
        return out.isEmpty() ? "custom" : out.toString();
    }
}
