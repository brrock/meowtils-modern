package meowtils.tenacitygui.tenacity.panel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import meowtils.tenacitygui.tenacity.Screen;
import meowtils.tenacitygui.tenacity.TenacityClickGUI;
import meowtils.tenacitygui.tenacity.adapter.ValueAdapter;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.anim.impl.EaseInOutQuad;
import meowtils.tenacitygui.tenacity.anim.impl.EaseOutSine;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.settings.KeybindComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.Theme;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.Pair;
import meowtils.tenacitygui.tenacity.util.TooltipObject;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.meowtils.GUI;

public class ModuleRect implements Screen {
   public final Module module;
   private int searchScore;
   private final Animation toggleAnimation = new EaseInOutQuad(300, 1.0);
   private final Animation hoverAnimation = new EaseOutSine(400, 1.0, Direction.BACKWARDS);
   private final Animation settingAnimation = new DecelerateAnimation(250, 1.0).setDirection(Direction.BACKWARDS);
   public final TooltipObject tooltipObject = new TooltipObject();
   private boolean expanded;
   private boolean typing;
   public float x;
   public float y;
   public float width;
   public float height;
   public float panelLimitY;
   public float alpha;
   private double settingSize = 1.0;
   private final List<SettingComponent> settingComponents;
   private double actualSettingCount;

   public ModuleRect(Module module) {
      this.module = module;
      this.settingComponents = new ArrayList<>();
      if (!module.alwaysEnabled) {
         this.settingComponents.add(new KeybindComponent(new KeybindComponent.BindAccess() {
            @Override
            public String name() {
               return "Bind";
            }

            @Override
            public int get() {
               return wtf.tatp.meowtils.extension.render.Keyboard.legacy(ModuleRect.this.module.getKey());
            }

            @Override
            public void set(int code) {
               ModuleRect.this.module.setKey(wtf.tatp.meowtils.extension.render.Keyboard.modern(code));
            }
         }));
      }

      this.settingComponents.addAll(ValueAdapter.build(module.getOrderedValues(), module));
   }

   public int getSearchScore() {
      return this.searchScore;
   }

   public void setSearchScore(int searchScore) {
      this.searchScore = searchScore;
   }

   public double getSettingSize() {
      return this.settingSize;
   }

   public boolean isTyping() {
      return this.typing;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   @Override
   public void initGui() {
      this.settingAnimation.setDirection(Direction.BACKWARDS);
      this.toggleAnimation.setDirection(Direction.BACKWARDS);
      if (this.settingComponents != null) {
         for (SettingComponent component : this.settingComponents) {
            component.initGui();
         }
      }
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      if (this.expanded) {
         for (SettingComponent settingComponent : this.settingComponents) {
            settingComponent.keyTyped(typedChar, keyCode);
         }
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      this.toggleAnimation.setDirection(this.module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
      this.settingAnimation.setDirection(this.expanded ? Direction.FORWARDS : Direction.BACKWARDS);
      boolean hoveringModule = HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY);
      this.hoverAnimation.setDirection(hoveringModule ? Direction.FORWARDS : Direction.BACKWARDS);
      this.hoverAnimation.setDuration(hoveringModule ? 250 : 400);
      boolean hoveringText = HoveringUtil.isHovering(
         this.x + 5.0F,
         this.y + Fonts.tenacityFont18.getMiddleOfBox(this.height),
         Fonts.tenacityFont18.getStringWidth(this.module.getName()),
         Fonts.tenacityFont18.getHeight(),
         mouseX,
         mouseY
      );
      this.tooltipObject.setTip(this.module.getTooltip());
      this.tooltipObject.setHovering(hoveringText && this.module.getTooltip() != null && tooltipsEnabled());
      Theme theme = Theme.getCurrentTheme();
      Pair<Color, Color> themeColors = theme.getColors();
      Pair<Color, Color> colors = Pair.of(
         ColorUtil.applyOpacity(themeColors.getFirst(), this.alpha), ColorUtil.applyOpacity(themeColors.getSecond(), this.alpha)
      );
      Color rectColor = new Color(35, 37, 43, (int)(255.0F * this.alpha));
      Color textColor = ColorUtil.applyOpacity(Color.WHITE, this.alpha);
      float textAlpha = 0.5F;
      Color moduleTextColor = ColorUtil.applyOpacity(textColor, textAlpha + 0.4F * this.toggleAnimation.getOutput().floatValue());
      if (this.module.getState() || !this.toggleAnimation.isDone()) {
         Color toggleColor = colors.getSecond();
         if (TenacityClickGUI.gradient) {
            toggleColor = ColorUtil.interpolateColorC(
               ColorUtil.applyOpacity(Color.BLACK, 0.15F), ColorUtil.applyOpacity(Color.WHITE, 0.12F), this.hoverAnimation.getOutput().floatValue()
            );
         }

         rectColor = ColorUtil.interpolateColorC(rectColor, toggleColor, this.toggleAnimation.getOutput().floatValue());
      }

      RenderUtil.resetColor();
      RenderUtil.drawRect2(
         this.x,
         this.y,
         this.width,
         this.height,
         ColorUtil.interpolateColor(rectColor, ColorUtil.brighter(rectColor, 0.8F), this.hoverAnimation.getOutput().floatValue())
      );
      RenderUtil.resetColor();
      Fonts.tenacityFont18
         .drawString(
            (this.module.getState() ? "§l" : "") + this.module.getName(),
            this.x + 5.0F,
            this.y + Fonts.tenacityFont18.getMiddleOfBox(this.height),
            moduleTextColor
         );
      Color settingRectColor = ColorUtil.tripleColor(32, this.alpha);
      float arrowX = this.x + this.width - 12.0F;
      if (this.settingComponents.size() > 0) {
         float arrowY = this.y + Fonts.iconFont20.getMiddleOfBox(this.height) + 1.0F;
         RenderUtil.rotateStart(
            arrowX, arrowY, Fonts.iconFont20.getStringWidth("z"), Fonts.iconFont20.getHeight(), 180.0F * this.settingAnimation.getOutput().floatValue()
         );
         Fonts.iconFont20.drawString("z", arrowX, arrowY, ColorUtil.applyOpacity(textColor, 0.5F));
         RenderUtil.rotateEnd();
      }

      double settingHeight = this.actualSettingCount * this.settingAnimation.getOutput();
      this.actualSettingCount = 0.0;
      this.typing = false;
      boolean clipSettings = !this.settingAnimation.isDone();
      if (this.expanded || clipSettings) {
         float settingRectHeight = 16.0F;
         RenderUtil.drawRect2(this.x, this.y + this.height, this.width, (float)(settingHeight * settingRectHeight), settingRectColor.getRGB());
         if (clipSettings) {
            RenderUtil.scissorStart(this.x, this.y + this.height, this.width, settingHeight * settingRectHeight);
         }

         for (SettingComponent settingComponent : this.settingComponents) {
            settingComponent.panelLimitY = this.panelLimitY;
            settingComponent.settingRectColor = settingRectColor;
            settingComponent.textColor = textColor;
            settingComponent.clientColors = colors;
            settingComponent.alpha = this.alpha;
            settingComponent.x = this.x;
            settingComponent.y = (float)(this.y + this.height + this.actualSettingCount * settingRectHeight);
            settingComponent.width = this.width;
            ValueAdapter.applyRowHeight(settingComponent, settingRectHeight);
            settingComponent.height = settingRectHeight * settingComponent.countSize;
            settingComponent.drawScreen(mouseX, mouseY);
            if (settingComponent.typing) {
               this.typing = true;
            }

            this.actualSettingCount = this.actualSettingCount + settingComponent.countSize;
         }

         if (clipSettings) {
            RenderUtil.scissorEnd();
         }
      }

      this.settingSize = settingHeight;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      boolean hoveringModule = this.isClickable(this.y, this.panelLimitY) && HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY);
      if (this.expanded && this.settingAnimation.finished(Direction.FORWARDS)) {
         for (SettingComponent settingComponent : this.settingComponents) {
            settingComponent.mouseClicked(mouseX, mouseY, button);
         }
      }

      if (hoveringModule) {
         switch (button) {
            case 0:
               this.toggleAnimation.setDirection(!this.module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
               this.module.toggle();
               break;
            case 1:
               this.expanded = !this.expanded;
         }
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
      if (this.expanded) {
         for (SettingComponent settingComponent : this.settingComponents) {
            settingComponent.mouseReleased(mouseX, mouseY, state);
         }
      }
   }

   public boolean isClickable(float y, float panelLimitY) {
      return y > panelLimitY && y < panelLimitY + SettingComponent.allowedClickGuiHeight;
   }

   public List<SettingComponent> getSettingComponents() {
      return this.settingComponents;
   }

   private static boolean tooltipsEnabled() {
      try {
         return ((GUI)Module.get(GUI.class)).tooltips;
      } catch (Throwable var1) {
         return true;
      }
   }
}
