package meowtils.tenacitygui.tenacity.panel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import meowtils.tenacitygui.tenacity.Screen;
import meowtils.tenacitygui.tenacity.TenacityClickGUI;
import meowtils.tenacitygui.tenacity.TenacityGuiModule;
import meowtils.tenacitygui.tenacity.adapter.MCategory;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.render.StencilUtil;
import meowtils.tenacitygui.tenacity.render.Theme;
import meowtils.tenacitygui.tenacity.search.FuzzySearch;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.MathUtils;
import meowtils.tenacitygui.tenacity.util.Pair;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.ScaledResolution;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.meowtils.GUI;

public class CategoryPanel implements Screen {
   private static final Minecraft mc = Minecraft.getInstance();
   private final MCategory category;
   private final float rectWidth = 105.0F;
   private final float categoryRectHeight = 15.0F;
   private boolean typing;
   public final Pair<Animation, Animation> openingAnimations;
   private List<ModuleRect> moduleRects;
   private int lastModuleCount = -1;
   float actualHeight = 0.0F;
   private float renderedHeight = 0.0F;
   private String searchText;
   private final List<ModuleRect> moduleRectFilter = new ArrayList<>();

   public CategoryPanel(MCategory category, Pair<Animation, Animation> openingAnimations) {
      this.category = category;
      this.openingAnimations = openingAnimations;
   }

   public boolean isTyping() {
      return this.typing;
   }

   public boolean isHidden() {
      return this.moduleRects != null && TenacityClickGUI.searchBar().isFiltering() && this.getModuleRects().isEmpty();
   }

   private List<Module> modulesInCategory() {
      List<Module> modules = new ArrayList<>();

      for (Module module : Module.getCategoryModules(this.category.category)) {
         if (GUI.shouldShowModule(module)) {
            modules.add(module);
         }
      }

      Collections.sort(modules, new Comparator<Module>() {
         public int compare(Module a, Module b) {
            return a.getName().compareTo(b.getName());
         }
      });
      return modules;
   }

   @Override
   public void initGui() {
      List<Module> modules = this.modulesInCategory();
      if (this.moduleRects == null || modules.size() != this.lastModuleCount) {
         this.moduleRects = new ArrayList<>();

         for (Module module : modules) {
            this.moduleRects.add(new ModuleRect(module));
         }

         this.lastModuleCount = modules.size();
      }

      for (ModuleRect rect : this.moduleRects) {
         rect.initGui();
      }
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      if (this.moduleRects != null) {
         for (ModuleRect rect : this.moduleRects) {
            rect.keyTyped(typedChar, keyCode);
         }
      }
   }

   @Override
   public void onDrag(int mouseX, int mouseY) {
      this.category.getDrag().onDraw(mouseX, mouseY);
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      if (this.moduleRects != null && !this.isHidden()) {
         if (this.openingAnimations != null) {
            TenacityGuiModule settings = TenacityGuiModule.get();
            float alpha = Math.min(1.0F, this.openingAnimations.getFirst().getOutput().floatValue());
            Theme theme = Theme.getCurrentTheme();
            Pair<Color, Color> clientColors = theme.getColors();
            float alphaValue = alpha * alpha;
            if (settings != null && settings.transparent) {
               alphaValue *= 0.75F;
            }

            Color clientFirst = ColorUtil.applyOpacity(clientColors.getFirst(), alphaValue);
            Color clientSecond = ColorUtil.applyOpacity(clientColors.getSecond(), alphaValue);
            int textColor = ColorUtil.applyOpacity(-1, alpha);
            float x = this.category.getDrag().getX();
            float y = this.category.getDrag().getY();
            if (settings != null && "Value".equals(settings.scrollMode)) {
               SettingComponent.allowedClickGuiHeight = settings.clickHeight;
            } else {
               ScaledResolution sr = new ScaledResolution(mc);
               SettingComponent.allowedClickGuiHeight = 2 * sr.func_78328_b() / 3.0F;
            }

            float allowedHeight = SettingComponent.allowedClickGuiHeight;
            boolean hoveringMods = HoveringUtil.isHovering(x, y + 15.0F, 105.0F, allowedHeight, mouseX, mouseY);
            RenderUtil.resetColor();
            float realHeight = Math.min(this.actualHeight, allowedHeight);
            this.renderedHeight = realHeight;
            boolean outlineAccent = settings != null && settings.outlineAccent;
            boolean transparent = settings != null && settings.transparent;
            boolean hasBody = realHeight > 0.0F;
            if (outlineAccent) {
               if (theme == Theme.RED_COFFEE) {
                  Color temp = clientFirst;
                  clientFirst = clientSecond;
                  clientSecond = temp;
               }

               if (TenacityClickGUI.gradient) {
                  RoundedUtil.drawGradientVertical(x - 0.75F, y - 0.5F, 106.5F, realHeight + 15.0F + 1.5F, 5.0F, clientFirst, clientSecond);
               } else {
                  RoundedUtil.drawRound(x - 0.75F, y - 0.5F, 106.5F, realHeight + 15.0F + 1.5F, 5.0F, clientFirst);
               }
            } else {
               RoundedUtil.drawRound(x - 0.75F, y - 0.5F, 106.5F, realHeight + 15.0F + 1.5F, 5.0F, ColorUtil.tripleColor(20, alphaValue));
               if (!transparent && hasBody) {
                  RenderUtil.drawRect2(x, y + 15.0F, 105.0, 3.0, clientFirst.getRGB());
               }

               if (hasBody) {
                  if (TenacityClickGUI.gradient) {
                     RoundedUtil.drawGradientVertical(x + 1.0F, y + 15.0F + 1.0F, 103.0F, realHeight - 2.0F, 4.0F, clientFirst, clientSecond);
                  } else {
                     RoundedUtil.drawRound(x + 0.8F, y + 15.0F + 0.8F, 103.4F, realHeight - 1.6F, 3.5F, clientFirst);
                  }
               }
            }

            StencilUtil.initStencilToWrite();
            RoundedUtil.drawRound(x + 1.0F, y + 15.0F + 5.0F, 103.0F, realHeight - 6.0F, 3.0F, Color.BLACK);
            RenderUtil.drawRect2(x, y + 15.0F, 105.0, 10.0, Color.BLACK.getRGB());
            StencilUtil.readStencilBuffer(1);
            double scroll = this.category.getScroll().getScroll();
            double count = 0.0;
            float rectHeight = 14.0F;

            for (ModuleRect moduleRect : this.getModuleRects()) {
               moduleRect.alpha = alpha;
               moduleRect.x = x - 0.5F;
               moduleRect.height = rectHeight;
               moduleRect.panelLimitY = y + 15.0F - 2.0F;
               moduleRect.y = (float)(y + 15.0F + count * rectHeight + MathUtils.roundToHalf(scroll));
               moduleRect.width = 106.0F;
               moduleRect.drawScreen(mouseX, mouseY);
               count += 1.0 + moduleRect.getSettingSize() * 1.1428572F;
            }

            this.typing = false;

            for (ModuleRect rect : this.getModuleRects()) {
               if (rect.isTyping()) {
                  this.typing = true;
                  break;
               }
            }

            this.actualHeight = (float)(count * rectHeight);
            if (hoveringMods) {
               this.category.getScroll().onScroll(25);
               float hiddenHeight = (float)(count * rectHeight - allowedHeight);
               this.category.getScroll().setMaxScroll(Math.max(0.0F, hiddenHeight));
            }

            StencilUtil.uninitStencilBuffer();
            RenderUtil.resetColor();
            RenderUtil.resetColor();
            float textWidth = Fonts.tenacityBoldFont22.getStringWidth(this.category.name + " ") / 2.0F;
            this.category.drawIcon(x + 52.5F + textWidth, y, 15.0F, textColor);
            RenderUtil.resetColor();
            Fonts.tenacityBoldFont22
               .drawString(
                  this.category.name, x + (52.5F - textWidth - this.category.iconWidth() / 2.0F), y + Fonts.tenacityBoldFont22.getMiddleOfBox(15.0F), textColor
               );
         }
      }
   }

   public void drawToolTips(int mouseX, int mouseY) {
      if (!this.isHidden()) {
         for (ModuleRect rect : this.getModuleRects()) {
            rect.tooltipObject.drawScreen(mouseX, mouseY);
         }
      }
   }

   public boolean isOverHeader(int mouseX, int mouseY) {
      return HoveringUtil.isHovering(this.category.getDrag().getX(), this.category.getDrag().getY(), 105.0F, 15.0F, mouseX, mouseY);
   }

   public boolean isOverPanel(int mouseX, int mouseY) {
      return HoveringUtil.isHovering(
         this.category.getDrag().getX() - 0.75F, this.category.getDrag().getY() - 0.5F, 106.5F, 15.0F + this.renderedHeight + 1.5F, mouseX, mouseY
      );
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      this.mouseClicked(mouseX, mouseY, button, true);
   }

   public void mouseClicked(int mouseX, int mouseY, int button, boolean focused) {
      if (!this.isHidden() && focused) {
         this.category.getDrag().onClick(mouseX, mouseY, button, this.isOverHeader(mouseX, mouseY));

         for (ModuleRect rect : this.getModuleRects()) {
            rect.mouseClicked(mouseX, mouseY, button);
         }
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
      this.category.getDrag().onRelease(state);
      if (!this.isHidden()) {
         for (ModuleRect rect : this.getModuleRects()) {
            rect.mouseReleased(mouseX, mouseY, state);
         }

         MCategory.savePositions();
      }
   }

   public List<ModuleRect> getModuleRects() {
      if (!TenacityClickGUI.searchBar().isFocused()) {
         return this.moduleRects;
      } else {
         String search = TenacityClickGUI.searchBar().getSearchField().getText();
         if (search.isEmpty()) {
            this.searchText = null;
            return this.moduleRects;
         } else if (search.equals(this.searchText)) {
            return this.moduleRectFilter;
         } else {
            this.searchText = search;
            this.moduleRectFilter.clear();

            for (ModuleRect moduleRect : this.moduleRects) {
               moduleRect.setSearchScore(FuzzySearch.weightedRatio(search, moduleRect.module.getName()));
            }

            for (ModuleRect rect : this.moduleRects) {
               if (rect.getSearchScore() > 60) {
                  this.moduleRectFilter.add(rect);
               }
            }

            Collections.sort(this.moduleRectFilter, new Comparator<ModuleRect>() {
               public int compare(ModuleRect a, ModuleRect b) {
                  return Integer.compare(b.getSearchScore(), a.getSearchScore());
               }
            });
            return this.moduleRectFilter;
         }
      }
   }
}
