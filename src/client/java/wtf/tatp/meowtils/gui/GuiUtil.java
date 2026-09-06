package wtf.tatp.meowtils.gui;

import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.gui.hudeditor.HudEditor;
import wtf.tatp.meowtils.module.meowtils.GUI;

public final class GuiUtil {
    private GuiUtil() {}
    public static boolean inEditor() { return Minecraft.getInstance().gui.screen() instanceof HudEditor; }
    public static float getScale() {
        Minecraft mc=Minecraft.getInstance(); GUI gui=Module.get(GUI.class);
        return GuiInteraction.scale(gui==null?"Auto":gui.scale,mc.getWindow().getWidth(),mc.getWindow().getGuiScale());
    }
    public static int[] getHudBounds(String longestText,int lineCount,float scale) {
        return new int[]{(int)Math.ceil(wtf.tatp.meowtils.font.HudFont.width(longestText,scale)),(int)Math.ceil((lineCount*9+Math.max(0,lineCount-1)*3)*scale)};
    }
}
