package wtf.tatp.meowtils.gui.hudeditor;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.gui.GuiPainter;
import wtf.tatp.meowtils.gui.ModuleManager;

public final class HudEditor extends Screen {
    private final List<HudEntry> entries;
    private HudEntry dragging;
    private double dx,dy;
    public HudEditor() {
        super(Component.literal("HUD Editor"));
        entries=ModuleManager.getModules().stream().filter(m->m.getState()).flatMap(m->m.hudEditor().stream()).toList();
    }
    private HudEntry hovered(double x, double y) {
        for (HudEntry e: entries.reversed()) { int[] b=e.getBounds();
            if (x>=e.getX() && x<=e.getX()+b[0] && y>=e.getY() && y<=e.getY()+b[1]) return e;
        }
        return null;
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        g.fill(0,0,width,height,0x30000000);
        HudEntry active=dragging!=null ? dragging : hovered(mx,my);
        if (active!=null) {
            int[] b=active.getBounds(); int x=active.getX(),y=active.getY();
            g.outline(x-1,y-1,b[0]+2,b[1]+2,0x5A000000);
            g.fill(x,y,x+b[0],y+b[1],0x1E505050);
            new GuiPainter(g,font).text(active.getName(),mx+6,my+12,6,-1);
        }
    }
    /** The editor must not blur HUD elements already extracted underneath this screen. */
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta) { }
    @Override protected void extractBlurredBackground(GuiGraphicsExtractor g) { }
    @Override public boolean isInGameUi() { return true; }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        if (e.button()!=0) return false;
        dragging=hovered(e.x(),e.y());
        if (dragging==null) return false;
        dx=e.x()-dragging.getX(); dy=e.y()-dragging.getY(); return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double deltaX,double deltaY) {
        if (dragging==null || e.button()!=0) return false;
        dragging.setX((int)(e.x()-dx)); dragging.setY((int)(e.y()-dy)); return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) {
        if (dragging==null) return false;
        dragging=null; ConfigManager.save(); return true;
    }
    @Override public void removed() { dragging=null; ConfigManager.save(); }
    @Override public boolean isPauseScreen() { return false; }
}
