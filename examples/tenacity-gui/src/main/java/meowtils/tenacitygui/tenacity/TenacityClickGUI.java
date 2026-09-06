package meowtils.tenacitygui.tenacity;

import java.awt.Color;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import wtf.tatp.meowtils.extension.render.Draw;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import meowtils.tenacitygui.tenacity.adapter.MCategory;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.EaseBackIn;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.ActionButton;
import meowtils.tenacitygui.tenacity.panel.CategoryPanel;
import meowtils.tenacitygui.tenacity.panel.ModuleRect;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.Theme;
import meowtils.tenacitygui.tenacity.search.SearchBar;
import meowtils.tenacitygui.tenacity.util.Pair;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.ScaledResolution;
import net.minecraft.resources.Identifier;
import wtf.tatp.meowtils.extension.render.Keyboard;
import wtf.tatp.meowtils.extension.render.Mouse;

import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEditor;
import wtf.tatp.meowtils.module.meowtils.GUI;

public class TenacityClickGUI extends Screen {
   private static TenacityClickGUI instance;
   private static final SearchBar SEARCH_BAR = new SearchBar();
   private final Pair<Animation, Animation> openingAnimations = Pair.of(new EaseBackIn(400, 1.0, 2.0F), new EaseBackIn(400, 0.4F, 2.0F));
   private List<CategoryPanel> categoryPanels;
   private final ActionButton hudEditorButton = new ActionButton("HUD Editor", () -> this.minecraft.gui.setScreen(new HudEditor()));
   public boolean binding;
   public static boolean gradient;
   private static final Identifier BLUR_SHADER = Identifier.fromNamespaceAndPath("minecraft", "shaders/post/blur.json");
   private boolean blurActive;
   private int eventButton = -1;
   private long lastMouseEvent;
   private static final float TARGET_GUI_SCALE = 2.0F;
   private static final int OFFSCREEN = -1073741824;

   public TenacityClickGUI() {
      super(Component.literal("Tenacity GUI"));
      instance = this;
   }

   public static SearchBar searchBar() {
      return SEARCH_BAR;
   }

   public static void collapseAll() {
      if (instance != null && instance.categoryPanels != null) {
         for (CategoryPanel panel : instance.categoryPanels) {
            for (ModuleRect rect : panel.getModuleRects()) {
               rect.setExpanded(false);
            }
         }
      }
   }

   public static void invalidate() {
      if (instance != null) {
         instance.categoryPanels = null;
      }

      MCategory.invalidate();
   }

   public void onDrag(int mouseX, int mouseY) {
      if (this.categoryPanels != null) {
         for (CategoryPanel catPanels : this.categoryPanels) {
            catPanels.onDrag(mouseX, mouseY);
         }
      }
   }

   @Override public void init() {
      Keyboard.enableRepeatEvents(true);
      Fonts.init();
      this.applyBlur();
      this.openingAnimations.use((fade, opening) -> {
         fade.setDirection(Direction.FORWARDS);
         opening.setDirection(Direction.FORWARDS);
      });
      if (this.categoryPanels == null) {
         this.categoryPanels = new ArrayList<>();

         for (MCategory category : MCategory.values()) {
            this.categoryPanels.add(new CategoryPanel(category, this.openingAnimations));
         }
      }

      SEARCH_BAR.initGui();

      for (CategoryPanel catPanels : this.categoryPanels) {
         catPanels.initGui();
      }
   }

   protected void func_73869_a(char typedChar, int keyCode) {
      if (keyCode != 1 || this.binding) {
         SEARCH_BAR.keyTyped(typedChar, keyCode);

         for (CategoryPanel categoryPanel : this.categoryPanels) {
            categoryPanel.keyTyped(typedChar, keyCode);
         }
      } else if (SEARCH_BAR.isFocused()) {
         SEARCH_BAR.getSearchField().setText("");
         SEARCH_BAR.getSearchField().setFocused(false);
      } else {
         SEARCH_BAR.getOpenAnimation().setDirection(Direction.BACKWARDS);
         this.openingAnimations.use((fade, opening) -> {
            fade.setDirection(Direction.BACKWARDS);
            opening.setDirection(Direction.BACKWARDS);
         });
      }
   }

   @Override public boolean isPauseScreen() {
      return false;
   }

   public void func_146274_d() throws IOException {

   }

   public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
      if (this.categoryPanels == null) {
         this.init();
      }

      this.binding = false;

      for (CategoryPanel panel : this.categoryPanels) {
         if (panel.isTyping()) {
            this.binding = true;
            break;
         }
      }

      this.binding = this.binding || SEARCH_BAR.isTyping();
      ScaledResolution sr = new ScaledResolution(this.minecraft);
      mouseX = this.layoutMouseX(mouseX, sr);
      mouseY = this.layoutMouseY(mouseY, sr);
      this.onDrag(mouseX, mouseY);
      if (this.openingAnimations.getSecond().finished(Direction.BACKWARDS)) {
         this.minecraft.gui.setScreen(null);
      } else {
         TenacityGuiModule settings = TenacityGuiModule.get();
         gradient = Theme.getCurrentTheme().isGradient();
         boolean focusedConfigGui = SEARCH_BAR.isTyping();
         int fakeMouseX = focusedConfigGui ? 0 : mouseX;
         int fakeMouseY = focusedConfigGui ? 0 : mouseY;
         float layoutScale = this.layoutScale(sr);
         if (layoutScale != 1.0F) {
            RenderUtil.scaleStart(sr.func_78326_a() / 2.0F, sr.func_78328_b() / 2.0F, layoutScale);
         }

         try {
            RenderUtil.scaleStart(sr.func_78326_a() / 2.0F, sr.func_78328_b() / 2.0F, this.openingAnimations.getSecond().getOutput().floatValue() + 0.6F);
            CategoryPanel hovered = this.topPanelAt(fakeMouseX, fakeMouseY);

            for (CategoryPanel catPanels : this.categoryPanels) {
               boolean visible = catPanels == hovered;
               catPanels.drawScreen(visible ? fakeMouseX : OFFSCREEN, visible ? fakeMouseY : OFFSCREEN);
            }

            RenderUtil.scaleEnd();

            for (CategoryPanel categoryPanel : this.categoryPanels) {
               boolean visible = categoryPanel == hovered;
               categoryPanel.drawToolTips(visible ? fakeMouseX : OFFSCREEN, visible ? fakeMouseY : OFFSCREEN);
            }

            SEARCH_BAR.setAlpha(this.openingAnimations.getFirst().getOutput().floatValue());
            SEARCH_BAR.drawScreen(fakeMouseX, fakeMouseY);
            this.layoutHudEditorButton(sr);
            this.hudEditorButton.alpha = this.openingAnimations.getFirst().getOutput().floatValue();
            this.hudEditorButton.setColor(new Color(35, 37, 43));
            this.hudEditorButton.drawScreen(mouseX, mouseY);
         } finally {
            if (layoutScale != 1.0F) {
               RenderUtil.scaleEnd();
            }
         }
      }
   }

   private float layoutScale(ScaledResolution sr) {
      return TARGET_GUI_SCALE / Math.max(1.0F, sr.func_78325_e());
   }

   private int layoutMouseX(int mouseX, ScaledResolution sr) {
      float scale = this.layoutScale(sr);
      if (scale == 1.0F) {
         return mouseX;
      }

      float center = sr.func_78326_a() / 2.0F;
      return Math.round(center + (mouseX - center) / scale);
   }

   private int layoutMouseY(int mouseY, ScaledResolution sr) {
      float scale = this.layoutScale(sr);
      if (scale == 1.0F) {
         return mouseY;
      }

      float center = sr.func_78328_b() / 2.0F;
      return Math.round(center + (mouseY - center) / scale);
   }

   private int[] layoutMouse(int mouseX, int mouseY) {
      ScaledResolution sr = new ScaledResolution(this.minecraft);
      return new int[]{this.layoutMouseX(mouseX, sr), this.layoutMouseY(mouseY, sr)};
   }

   private CategoryPanel topPanelAt(int mouseX, int mouseY) {
      for (int i = this.categoryPanels.size() - 1; i >= 0; i--) {
         CategoryPanel panel = this.categoryPanels.get(i);
         if (!panel.isHidden() && panel.isOverPanel(mouseX, mouseY)) {
            return panel;
         }
      }

      return null;
   }

   private void layoutHudEditorButton(ScaledResolution sr) {
      this.hudEditorButton.width = this.hudEditorButton.preferredWidth(7.0F);
      this.hudEditorButton.height = 16.0F;
      this.hudEditorButton.x = 6.0F;
      this.hudEditorButton.y = sr.func_78328_b() - (this.hudEditorButton.height + 6.0F);
   }

   protected void func_73864_a(int mouseX, int mouseY, int mouseButton) {
      int[] mouse = this.layoutMouse(mouseX, mouseY);
      mouseX = mouse[0];
      mouseY = mouse[1];
      this.layoutHudEditorButton(new ScaledResolution(this.minecraft));
      this.hudEditorButton.mouseClicked(mouseX, mouseY, mouseButton);
      SEARCH_BAR.mouseClicked(mouseX, mouseY, mouseButton);
      CategoryPanel target = this.topPanelAt(mouseX, mouseY);

      for (CategoryPanel cat : new ArrayList<>(this.categoryPanels)) {
         cat.mouseClicked(mouseX, mouseY, mouseButton, cat == target);
      }

      if (target != null) {
         this.categoryPanels.remove(target);
         this.categoryPanels.add(target);
      }
   }

   protected void func_146286_b(int mouseX, int mouseY, int state) {
      int[] mouse = this.layoutMouse(mouseX, mouseY);
      mouseX = mouse[0];
      mouseY = mouse[1];
      SEARCH_BAR.mouseReleased(mouseX, mouseY, state);

      for (CategoryPanel cat : this.categoryPanels) {
         cat.mouseReleased(mouseX, mouseY, state);
      }
   }

   private void applyBlur() {

   }

   private void clearBlur() {

   }

   @Override public void removed() {
      Keyboard.enableRepeatEvents(false);
      MCategory.savePositions();
      TenacityConfig.forceSave();
      wtf.tatp.meowtils.config.ConfigManager.save();
      this.clearBlur();
   }
   @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta){
      Draw.begin(graphics);
      try { func_73863_a(mouseX,mouseY,delta); } finally { Draw.end(); Mouse.wheel=0; }
   }
   @Override public void extractBackground(GuiGraphicsExtractor graphics,int x,int y,float delta){
      GUI settings=Module.get(GUI.class);if(settings!=null && settings.blurGui)graphics.blurBeforeThisStratum();
   }
   @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){func_73864_a((int)event.x(),(int)event.y(),event.button());return true;}
   @Override public boolean mouseReleased(MouseButtonEvent event){func_146286_b((int)event.x(),(int)event.y(),event.button());return true;}
   @Override public boolean mouseDragged(MouseButtonEvent event,double x,double y){int[] mouse=this.layoutMouse((int)event.x(),(int)event.y());onDrag(mouse[0],mouse[1]);return true;}
   @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){Mouse.wheel+=(int)(vertical*120);return true;}
   @Override public boolean keyPressed(KeyEvent event){func_73869_a('\0',Keyboard.legacy(event.key()));return true;}
   @Override public boolean charTyped(CharacterEvent event){for(char c:Character.toChars(event.codepoint()))func_73869_a(c,0);return true;}
   @Override public boolean shouldCloseOnEsc(){return false;}

}
