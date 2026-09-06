package wtf.tatp.meowtils.extension.render;
import java.awt.image.BufferedImage;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
public final class DynamicTexture {
    private static final Map<Integer,Identifier> ids=new HashMap<>();
    private static final Map<Integer,ClassLoader> owners=new HashMap<>();
    private static int nextId;
    private static final String SESSION=UUID.randomUUID().toString();
    private final int id;
    public DynamicTexture(BufferedImage image) {
        id=++nextId;
        ClassLoader own=DynamicTexture.class.getClassLoader();
        ClassLoader owner=StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames->frames.map(f->f.getDeclaringClass().getClassLoader()).filter(loader->loader!=null && loader!=own).findFirst().orElse(own));
        owners.put(id,owner);
        Identifier name=Identifier.fromNamespaceAndPath("meowtils","extension/"+SESSION+"/"+id);
        NativeImage pixels=new NativeImage(image.getWidth(),image.getHeight(),false);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)pixels.setPixel(x,y,image.getRGB(x,y));
        Minecraft.getInstance().getTextureManager().register(name,new net.minecraft.client.renderer.texture.DynamicTexture(()->"Extension UI",pixels));ids.put(id,name);
    }
    public int func_110552_b() { return id; }
    public static Identifier identifier(int id) { return ids.get(id); }
    public static void releaseLoader(ClassLoader loader) {
        for(var entry:new HashMap<>(owners).entrySet())if(entry.getValue()==loader){var name=ids.remove(entry.getKey());owners.remove(entry.getKey());if(name!=null)Minecraft.getInstance().getTextureManager().release(name);}
    }
    public static void closeAll() { for(var id:ids.values())Minecraft.getInstance().getTextureManager().release(id);ids.clear();owners.clear(); }
}
