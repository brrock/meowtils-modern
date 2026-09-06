package wtf.tatp.meowtils.gui;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.config.GuiConfig.FrameState;
import wtf.tatp.meowtils.gui.values.*;
import wtf.tatp.meowtils.gui.hudeditor.HudEditor;
import wtf.tatp.meowtils.module.meowtils.GUI;

/** Original floating-frame GUI, using 26.2 extraction and input events. */
public final class ClickGuiScreen extends Screen {
    private final List<Frame> frames=new ArrayList<>();
    private final Set<Module> expanded=Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Row> rows=new ArrayList<>();
    private Frame dragging;
    private double dragX,dragY;
    private Row track;
    private TextValue typing;
    private BindValue binding;
    private Module moduleBinding;
    private Row dropdown;
    private Module hovered;
    private long hoverSince;
    private String featureMode;
    private float scale=1;
    private record Row(Frame frame, Module module, Value<?> value, int y, int depth, String background) {
        int x() { return frame.state.x; }
        boolean hit(double mx,double my) { return mx>=x() && mx<x()+82 && my>=y && my<y+(value==null?15:12); }
    }
    private static final class Frame {
        final Module.Category category;
        final FrameState state;
        double scroll,target;
        int height=11;
        Frame(Module.Category category) {
            this.category=category; state=ConfigManager.guiConfig.frame(category.name(),category.ordinal());
        }
        int y() { return state.y+(int)scroll; }
    }
    public ClickGuiScreen() {
        super(Component.literal("Meowtils"));
        for (Module.Category category:Module.Category.values()) frames.add(new Frame(category));
    }
    private GUI settings() { return Module.get(GUI.class); }
    private void updateScale() {
        GUI gui=settings();
        scale=GuiInteraction.scale(gui==null?"Auto":gui.scale,minecraft.getWindow().getWidth(),minecraft.getWindow().getGuiScale());
    }
    @Override protected void init() { updateScale(); layout(); }
    private List<Module> modules(Frame frame) {
        return Module.getCategoryModules(frame.category).stream().filter(GUI::shouldShowModule).toList();
    }
    private boolean visible(Frame frame) { return frame.category!=Module.Category.Extensions || !modules(frame).isEmpty(); }
    private void layout() {
        rows.clear();
        for (Frame frame:frames) {
            if (!visible(frame)) continue;
            int offset=11;
            List<Module> modules=modules(frame);
            if (frame.state.open) for (int i=0;i<modules.size();i++) {
                Module module=modules.get(i);
                rows.add(new Row(frame,module,null,frame.y()+offset+1,0,""));
                if (expanded.contains(module)) {
                    offset+=12;
                    offset=values(frame,module,module.getOrderedValues(),offset,0,i==modules.size()-1);
                    offset+=3;
                } else offset+=15;
            }
            if (offset<frame.height) { frame.scroll=0; frame.target=0; }
            frame.height=offset;
        }
        // Reattach overlays to the latest geometry after scrolling/dragging.
        if (dropdown!=null) {
            Value<?> value=dropdown.value;
            dropdown=rows.stream().filter(r->r.value==value).findFirst().orElse(null);
        }
        if (typing!=null && rows.stream().noneMatch(r->r.value==typing)) typing=null;
        if (binding!=null && rows.stream().noneMatch(r->r.value==binding)) binding=null;
    }
    private int values(Frame frame,Module module,List<Object> values,int offset,int depth,boolean lastModule) {
        List<Value<?>> list=new ArrayList<>();
        for (Object raw:values) if (raw instanceof Value<?> value) list.add(value);
        for (int i=0;i<list.size();i++) {
            Value<?> value=list.get(i); boolean first=i==0,last=i==list.size()-1;
            boolean open=value instanceof ExpandValue e && e.getState();
            String background=depth>0 ? (last && lastModule?"expandpart/expand_background_last":"expandpart/expand_background")
                    : open && last ? "sub_component_background"
                    : first && last ? (lastModule?"sub_component_background_only_last":"sub_component_background_only")
                    : first ? "sub_component_background_top"
                    : last ? (lastModule?"sub_component_background_last":"sub_component_background_bottom")
                    : "sub_component_background";
            rows.add(new Row(frame,module,value,frame.y()+offset+4,depth,background));
            offset+=12;
            if (open) offset=values(frame,module,((ExpandValue)value).getSubValues(),offset,depth+1,lastModule && last);
        }
        return offset;
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        updateScale();
        GUI gui=settings();
        if (gui!=null && !Objects.equals(featureMode,gui.featureMode)) {
            if (featureMode!=null) for (Module m:ModuleManager.getModules()) if (!GUI.shouldShowModule(m)) m.setState(false);
            featureMode=gui.featureMode;
        }
        for (Frame f:frames) {
            if (f.scroll<f.target) f.scroll=Math.min(f.scroll+1,f.target);
            else if (f.scroll>f.target) f.scroll=Math.max(f.scroll-1,f.target);
        }
        layout();
        double mx=mouseX/scale,my=mouseY/scale;
        int accent=gui==null?0xFFBD8CFF:gui.accent();
        GuiPainter p=new GuiPainter(g,font);
        p.texture("hud_button",6,height-20);
        p.text("HUD Editor",25,height-10,7,-1);
        String version=wtf.tatp.meowtils.BuildInfo.displayName();
        p.text(version,width-p.textWidth(version,7)-6,height-2,7,accent);
        g.pose().pushMatrix(); g.pose().scale(scale,scale);
        Module currentHover=null;
        for (Frame frame:frames) {
            if (!visible(frame)) continue;
            // Separate strata preserve frame stacking in the deferred renderer.
            g.nextStratum();
            int x=frame.state.x,y=frame.y();
            p.texture(frame.state.open && !modules(frame).isEmpty()?"category_expanded":"category_not_expanded",x-1,y-3);
            p.text(frame.category.name(),x+15,y+9,9,-1);
            p.texture("icons/"+frame.category.name().toLowerCase(Locale.ROOT),x+3,y+1,9,9,-1);
            p.texture(frame.state.open?"arrow_down":"arrow_up",x+70,y+(frame.state.open?2:3),5,5,-1);
            for (Row row:rows) if (row.frame==frame) {
                boolean hover=row.hit(mx,my) && topFrame(mx,my)==frame;
                if (row.value==null) { renderModule(g,p,row,hover,accent); if (hover) currentHover=row.module; }
                else renderValue(g,p,row,hover,accent);
            }
        }
        if (dropdown!=null) {
            g.nextStratum();
            ModeValue mode=(ModeValue)dropdown.value;
            List<String> choices=mode.getModes().stream().filter(m->!m.equals(mode.getMode())).toList();
            for (int i=0;i<choices.size();i++) {
                int y=dropdown.y+(i+1)*10;
                boolean hover=mx>=dropdown.x() && mx<dropdown.x()+80 && my>=y && my<y+10;
                modeBox(p,dropdown.x()-1,y,choices.get(i),i==choices.size()-1?"bottom":"middle",hover,accent);
            }
        }
        g.pose().popMatrix();
        if (currentHover!=hovered) { hovered=currentHover; hoverSince=System.currentTimeMillis(); }
        if (gui!=null && gui.tooltips && hovered!=null && dropdown==null && System.currentTimeMillis()-hoverSince>=250) tooltip(g,p,mouseX,mouseY);
    }
    private void renderModule(GuiGraphicsExtractor g,GuiPainter p,Row row,boolean hover,int accent) {
        List<Module> list=modules(row.frame); int i=list.indexOf(row.module);
        boolean above=i>0,below=i<list.size()-1,open=expanded.contains(row.module);
        p.texture(below || open?"module_disabled":"module_disabled_bottom",row.x()-1,row.y);
        if (row.module.getState()) {
            boolean ea=above && list.get(i-1).getState(),eb=below && list.get(i+1).getState();
            p.texture(ea && eb?"module_connected_both":ea?"module_connected_top":eb?"module_connected_bottom":"module_not_connected",row.x()-1,row.y,accent);
        }
        int color=row.module.getState() && (((accent>>16)&255)*.299+((accent>>8)&255)*.587+(accent&255)*.114)>180 ? 0xFF282828 : -1;
        p.text(moduleBinding==row.module?"Bind.. "+keyName(row.module.getKey()):row.module.getName(),row.x()+3,row.y+9,6,color,color==0xFF282828?.1f:.4f);
        String arrow=row.module.getOrderedValues().isEmpty()?"dots":open?"module_arrow_down":"module_arrow_up";
        p.texture(arrow+(hover?"_hover":""),row.x()+70,row.y+5,5,5,row.module.getState()?-1:0xFF969696);
    }
    private void renderValue(GuiGraphicsExtractor g,GuiPainter p,Row row,boolean hover,int accent) {
        int x=row.x()-1,y=row.y;
        Value<?> value=row.value;
        p.texture(row.background,x,y);
        if (value instanceof CheckValue check) {
            p.texture("check_disabled"+(hover?"_hover":""),x+1,y,12,12,-1);
            if (check.get()) p.texture("check_enabled"+(hover?"_hover":""),x+1,y,12,12,accent);
            p.text(value.getName(),x+13,y+8,5,-1);
        } else if (value instanceof ToggleValue toggle) {
            p.texture("boolean_disabled"+(hover?"_hover":""),x+69,y,12,12,-1);
            if (toggle.get()) {
                p.texture("boolean_enabled"+(hover?"_hover":""),x+69,y,12,12,accent);
                p.texture("boolean_enabled_button"+(hover?"_hover":""),x+69,y,12,12,-1);
            }
            p.text(value.getName(),x+3,y+8,5,-1);
        } else if (value instanceof ModeValue mode) {
            boolean open=dropdown!=null && dropdown.value==value;
            modeBox(p,x,y,value.getName()+" - "+mode.getMode(),open?"top":"not_expanded",hover,accent);
            p.texture("modepart/mode_arrow_"+(open?"down":"side"),x+72,y+4,5,5,-1);
        } else if (value instanceof TextValue text) {
            p.texture("text_part",x,y); p.texture("text_part_overlay",x,y,accent);
            if (hover) p.texture("text_part_hover",x,y);
            String prefix=value.getName().isEmpty()?"":value.getName()+": ";
            String shown=text.get().isEmpty() && typing!=text?text.getDescription():text.get();
            while (!shown.isEmpty() && p.textWidth(prefix+shown,5)>73) shown=shown.substring(shown.offsetByCodePoints(0,1));
            p.text(prefix+shown+(typing==text && (System.currentTimeMillis()/500)%2==0?"|":""),x+5,y+8,5,text.get().isEmpty() && typing!=text?0xFF888888:-1);
        } else if (value instanceof BindValue bind) {
            p.text(value.getName()+": "+(binding==bind?"...":keyName(bind.getBind())),x+3,y+8,5,-1);
        } else if (value instanceof ExpandValue expand) {
            p.texture("expandpart/expand_"+(expand.getState()?"down":"side")+(hover?"_hover":""),x+3,y+3,5,5,-1);
            p.text(value.getName(),x+12,y+8,5,-1);
        } else if (value instanceof ButtonValue button) {
            p.texture(hover?"button_part_hover":"button_part",x,y);
            p.texture("button_part_border",x,y,accent);
            p.text(value.getName(),x+41-p.textWidth(value.getName(),button.getScale())/2,y+8,button.getScale(),-1);
        } else if (GuiInteraction.isTrack(value)) renderTrack(g,p,row,hover,accent);
    }
    private void renderTrack(GuiGraphicsExtractor g,GuiPainter p,Row row,boolean hover,int accent) {
        Value<?> value=row.value; int x=row.x()-1,y=row.y;
        double percent=0;
        boolean color=value instanceof ColorValue || value instanceof SaturationValue || value instanceof BrightnessValue || value instanceof OpacityValue;
        if (value instanceof ColorValue c) {
            percent=c.getLink().getHue();
            p.texture("colorpart/color_track"+(hover?"_hover":""),x,y);
        } else if (value instanceof SaturationValue s) {
            percent=s.getValue();
            p.texture("colorpart/blank_track"+(hover?"_hover":""),x,y,0xFF000000|s.getLink().getPureHueRGB());
            p.texture("colorpart/saturation_fade"+(hover?"_hover":""),x,y);
        } else if (value instanceof BrightnessValue b) {
            percent=b.getValue();
            p.texture("colorpart/blank_track"+(hover?"_hover":""),x,y,0xFF000000|b.getLink().getPureHueRGB());
            p.texture("colorpart/brightness_fade"+(hover?"_hover":""),x,y);
        } else if (value instanceof SliderValue slider) {
            percent=(slider.get()-slider.getMin())/Math.max(.0001,slider.getMax()-slider.getMin());
            if (value instanceof OpacityValue) {
                p.texture("colorpart/opacity_track"+(hover?"_hover":""),x,y);
                p.texture("colorpart/track_overlay"+(hover?"_hover":""),x,y,accent);
            } else {
                p.texture("sliderpart/slider_track",x,y); p.texture("sliderpart/slider_end",x,y,accent);
                g.fill(x+5,y+7,x+5+(int)(percent*74),y+10,accent);
                String label=slider.getFormattedValue()+(slider.getValueType()==null?"":" "+slider.getValueType());
                p.text(label,x+79-p.textWidth(label,5),y+5,5,-1);
            }
        }
        if (color) {
            p.texture("colorpart/color_button",x+(int)(percent*75),y+4,8,8,-1);
            p.text(value.getName(),x+4,y+5,4.5f,-1);
        } else {
            p.texture("sliderpart/slider_button"+(hover?"_large":""),x+3+(int)(percent*74),y+6,5,5,-1);
            p.text(value.getName(),x+3,y+5,5,-1);
        }
    }
    private void modeBox(GuiPainter p,int x,int y,String text,String part,boolean hover,int accent) {
        p.texture("modepart/mode_"+part+(hover?"_hover":""),x,y);
        p.texture("modepart/mode_"+part+"_outline",x,y,accent);
        p.plainText(text,x+5,y+8,5,-1);
    }
    private void tooltip(GuiGraphicsExtractor g,GuiPainter p,int mx,int my) {
        String text=hovered.getTooltip();
        if (hovered.getPortStatus()!=null) text=(text==null?"":text)+"\n§e"+hovered.getPortStatus();
        if (text==null || text.isBlank()) return;
        float ts=scale/2;
        int sw=(int)(width/ts),sh=(int)(height/ts),x=(int)((mx+6)/ts),y=(int)(my/ts);
        int available=Math.max(80,sw-x-8);
        List<String> lines=new ArrayList<>();
        for (String manual:text.split("\n")) {
            String line="";
            for (String word:manual.split(" ")) {
                String next=line.isEmpty()?word:line+" "+word;
                if (!line.isEmpty() && p.textWidth(next,10.5f)>available) { lines.add(line); line=word; }
                else line=next;
            }
            lines.add(line);
        }
        int w=(int)Math.ceil(lines.stream().mapToDouble(l->p.textWidth(l,10.5f)).max().orElse(1)),h=lines.size()*11;
        x=Math.max(3,Math.min(x,sw-w-8)); y=Math.max(3,Math.min(y,sh-h-8));
        g.nextStratum(); g.pose().pushMatrix(); g.pose().scale(ts,ts);
        g.fill(x-3,y-3,x+w+3,y+h+1,0xFF0E0E0E);
        for (int i=0;i<lines.size();i++) p.plainText(lines.get(i),x,y+i*11+7,10.5f,-1);
        g.pose().popMatrix();
    }
    private Frame topFrame(double x,double y) {
        for (Frame f:frames.reversed()) if (visible(f) && x>=f.state.x-1 && x<f.state.x+83 && y>=f.y()-3 && y<f.y()+f.height+5) return f;
        return null;
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice) {
        updateScale(); layout();
        if (e.button()==0 && e.x()>=6 && e.x()<=78 && e.y()>=height-20 && e.y()<=height-7) {
            ConfigManager.save(); minecraft.setScreenAndShow(new HudEditor()); return true;
        }
        double mx=e.x()/scale,my=e.y()/scale;
        typing=null; binding=null; moduleBinding=null;
        if (dropdown!=null) {
            ModeValue mode=(ModeValue)dropdown.value;
            List<String> choices=mode.getModes().stream().filter(m->!m.equals(mode.getMode())).toList();
            if (mx>=dropdown.x() && mx<dropdown.x()+80 && my>=dropdown.y+10 && my<dropdown.y+(choices.size()+1)*10) {
                if (e.button()==0) mode.setMode(choices.get((int)((my-dropdown.y)/10)-1));
                dropdown=null; changed(); return true;
            }
            if (dropdown.hit(mx,my)) { dropdown=null; return true; }
            dropdown=null;
        }
        Frame frame=topFrame(mx,my);
        if (frame==null) return false;
        if (my<frame.y()+11) {
            if (e.button()==0) { dragging=frame; dragX=mx-frame.state.x; dragY=my-frame.y(); frame.state.y=frame.y(); frame.scroll=frame.target=0; frames.remove(frame); frames.add(frame); }
            if (e.button()==1) { frame.state.open=!frame.state.open; frame.scroll=frame.target=0; changed(); }
            return true;
        }
        for (Row row:rows.reversed()) if (row.frame==frame && row.hit(mx,my)) {
            if (row.value==null) {
                if (e.button()==0) row.module.toggle();
                if (e.button()==1 && !row.module.getOrderedValues().isEmpty()) { if (!expanded.remove(row.module)) expanded.add(row.module); }
                if (e.button()==2 && (!row.module.alwaysEnabled || row.module instanceof GUI)) moduleBinding=row.module;
            } else if (e.button()==0) {
                Value<?> value=row.value;
                if (value instanceof ToggleValue toggle) toggle.toggle();
                else if (value instanceof ModeValue) dropdown=row;
                else if (value instanceof TextValue text) typing=text;
                else if (value instanceof BindValue bind) binding=bind;
                else if (value instanceof ExpandValue expand) expand.toggle();
                else if (value instanceof ButtonValue button) button.press();
                else if (GuiInteraction.isTrack(value)) { track=row; GuiInteraction.drag(value,mx,row.x()+4,74); }
            }
            changed(); return true;
        }
        return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy) {
        double x=e.x()/scale,y=e.y()/scale;
        if (dragging!=null && e.button()==0) {
            dragging.state.x=(int)(x-dragX); dragging.state.y=(int)(y-dragY); return true;
        }
        if (track!=null && e.button()==0) { GuiInteraction.drag(track.value,x,track.x()+4,74); return true; }
        return false;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) {
        boolean handled=dragging!=null || track!=null; dragging=null; track=null; if (handled) changed(); return handled;
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        Frame frame=topFrame(x/scale,y/scale); if (frame==null) return false;
        int speed=settings()==null?10:settings().scrollSpeed;
        frame.target+=Math.signum(vertical)*speed;
        frame.target=Math.max(-Math.max(0,frame.height-11-200/scale),Math.min(0,frame.target));
        return true;
    }
    @Override public boolean keyPressed(KeyEvent e) {
        int key=e.key();
        if (binding!=null || moduleBinding!=null) {
            boolean clear=key==GLFW.GLFW_KEY_BACKSPACE || key==GLFW.GLFW_KEY_ESCAPE || key==GLFW.GLFW_KEY_DELETE
                    || moduleBinding!=null && key==GLFW.GLFW_KEY_LEFT_SHIFT;
            if (binding!=null) binding.setBind(clear?0:key);
            if (moduleBinding!=null) moduleBinding.setKey(clear?0:key);
            binding=null; moduleBinding=null; changed(); return true;
        }
        if (typing!=null) {
            if (key==GLFW.GLFW_KEY_ESCAPE || key==GLFW.GLFW_KEY_ENTER || key==GLFW.GLFW_KEY_KP_ENTER) typing=null;
            else if (key==GLFW.GLFW_KEY_BACKSPACE) typing.set(GuiInteraction.backspace(typing.get()));
            else if (key==GLFW.GLFW_KEY_V && (e.modifiers() & (GLFW.GLFW_MOD_CONTROL|GLFW.GLFW_MOD_SUPER))!=0)
                typing.set(GuiInteraction.appendText(typing.get(),minecraft.keyboardHandler.getClipboard()));
            changed(); return true;
        }
        if (key==GLFW.GLFW_KEY_ESCAPE && dropdown!=null) { dropdown=null; return true; }
        return super.keyPressed(e);
    }
    @Override public boolean charTyped(CharacterEvent e) {
        if (typing==null) return false;
        typing.set(GuiInteraction.appendText(typing.get(),e.codepointAsString())); changed(); return true;
    }
    private void changed() { ConfigManager.save(); }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta) {
        GUI gui=settings();
        if (gui!=null && gui.blurGui) g.blurBeforeThisStratum();
    }
    private static String keyName(int key) {
        if (key==0) return "None";
        return com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
    }
    @Override public void removed() { dragging=null; track=null; typing=null; binding=null; moduleBinding=null; dropdown=null; ConfigManager.save(); }
    @Override public boolean isPauseScreen() { return false; }
}
