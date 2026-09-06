package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import java.util.List;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.ContinualAnimation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.render.Theme;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.Pair;
import wtf.tatp.meowtils.gui.values.ModeValue;

public class ModeComponent extends SettingComponent {
   private final ModeValue setting;
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation openAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation selectionBox = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private boolean opened;
   public float realHeight;
   public float normalCount;
   private final ContinualAnimation selectionBoxY = new ContinualAnimation();
   private String hoveringMode = "";

   public ModeComponent(ModeValue setting) {
      this.setting = setting;
      this.normalCount = 2.0F;
   }

   @Override
   public String getName() {
      return this.setting.getName();
   }

   @Override
   public void initGui() {
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      List<String> modes = this.setting.getModes();
      String current = this.setting.getValue();
      float boxHeight = 18.0F;
      float boxY = this.y + this.realHeight / 2.0F - boxHeight / 2.0F + 4.0F;
      float boxX = this.x + 5.0F;
      float boxWidth = this.width - 10.0F;
      boolean themeSetting = Theme.get(current) != null;
      boolean hoveringBox = HoveringUtil.isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY);
      this.hoverAnimation.setDirection(hoveringBox ? Direction.FORWARDS : Direction.BACKWARDS);
      this.openAnimation.setDirection(this.opened ? Direction.FORWARDS : Direction.BACKWARDS);
      Color outlineColor = ColorUtil.interpolateColorC(
         this.settingRectColor.brighter().brighter(), this.clientColors.getSecond(), 0.3F * this.hoverAnimation.getOutput().floatValue()
      );
      outlineColor = ColorUtil.interpolateColorC(outlineColor, this.clientColors.getSecond(), this.openAnimation.getOutput().floatValue());
      Color rectColor = ColorUtil.interpolateColorC(
         this.settingRectColor.brighter(),
         this.settingRectColor.brighter().brighter(),
         0.5F * this.hoverAnimation.getOutput().floatValue() + this.openAnimation.getOutput().floatValue()
      );
      RoundedUtil.drawRound(boxX, boxY, boxWidth, boxHeight, 4.0F, outlineColor);
      RoundedUtil.drawRound(boxX + 1.0F, boxY + 1.0F, boxWidth - 2.0F, boxHeight - 2.0F, 3.0F, rectColor);
      Fonts.tenacityFont14.drawString(this.setting.getName(), boxX + 1.0F, this.y + 3.0F, this.textColor);
      Fonts.tenacityFont16.drawString(current, boxX + 5.0F, boxY + Fonts.tenacityFont16.getMiddleOfBox(boxHeight), this.textColor);
      if (themeSetting) {
         Pair<Color, Color> themeColors = Theme.getThemeColors(current);
         Color first = ColorUtil.applyOpacity(themeColors.getFirst(), this.alpha);
         Color second = ColorUtil.applyOpacity(themeColors.getSecond(), this.alpha);
         float swatchHeight = 8.0F;
         float swatchWidth = 8.0F;
         float middleOfRect = boxHeight / 2.0F - swatchHeight / 2.0F;
         float spacing = 3.0F;
         RoundedUtil.drawRound(boxX + 7.5F + Fonts.tenacityFont16.getStringWidth(current), boxY + middleOfRect, swatchWidth, swatchHeight, 2.25F, first);
         RoundedUtil.drawRound(
            boxX + 7.5F + Fonts.tenacityFont16.getStringWidth(current) + (spacing + swatchWidth), boxY + middleOfRect, swatchWidth, swatchHeight, 2.25F, second
         );
      }

      RenderUtil.resetColor();
      RenderUtil.resetColor();
      float arrowX = boxX + boxWidth - 11.0F;
      float arrowY = boxY + Fonts.iconFont20.getMiddleOfBox(boxHeight) + 1.0F;
      float openAnim = this.openAnimation.getOutput().floatValue();
      RenderUtil.rotateStart(arrowX, arrowY, Fonts.iconFont20.getStringWidth("z"), Fonts.iconFont20.getHeight(), 180.0F * openAnim);
      Fonts.iconFont20.drawString("z", boxX + boxWidth - 11.0F, boxY + Fonts.iconFont20.getMiddleOfBox(boxHeight) + 1.0F, this.textColor);
      RenderUtil.rotateEnd();
      if (!this.opened && this.openAnimation.isDone()) {
         this.countSize = 2.0F;
      } else {
         float rectHeight = 15.0F;
         float rectCount = 0.0F;
         float modeHeight = (modes.size() - 1) * rectHeight;
         float modeY = boxY + boxHeight + 4.0F;
         float modeX = boxX - 0.25F;
         RoundedUtil.drawRound(
            modeX, modeY, boxWidth, Math.max(4.0F, modeHeight * openAnim), 4.0F, ColorUtil.applyOpacity(this.settingRectColor.brighter(), openAnim)
         );
         boolean mouseOutsideRect = mouseY < modeY || mouseY > modeY + modeHeight || mouseX < modeX || mouseX > modeX + boxWidth;
         this.selectionBox.setDirection(mouseOutsideRect ? Direction.BACKWARDS : Direction.FORWARDS);
         RoundedUtil.drawRound(
            modeX + 1.5F,
            modeY + 1.5F + this.selectionBoxY.getOutput(),
            boxWidth - 3.0F,
            rectHeight - 3.0F,
            2.5F,
            ColorUtil.applyOpacity(this.settingRectColor.brighter().brighter(), openAnim * this.selectionBox.getOutput().floatValue())
         );

         for (String mode : modes) {
            if (!mode.equals(current)) {
               boolean hoveringMode = HoveringUtil.isHovering(modeX, modeY + rectCount * rectHeight, boxWidth, rectHeight, mouseX, mouseY);
               if (hoveringMode) {
                  this.hoveringMode = mode;
               }

               if (mode.equals(this.hoveringMode)) {
                  this.selectionBoxY.animate(rectCount * rectHeight, 17);
               }

               RenderUtil.resetColor();
               Fonts.tenacityFont16
                  .drawString(
                     mode,
                     modeX + 5.0F,
                     modeY + (Fonts.tenacityFont16.getMiddleOfBox(rectHeight) + rectHeight * rectCount) * this.openAnimation.getOutput().floatValue(),
                     ColorUtil.applyOpacity(this.textColor, openAnim)
                  );
               if (themeSetting) {
                  Pair<Color, Color> themeColors = Theme.getThemeColors(mode);
                  Color first = ColorUtil.applyOpacity(themeColors.getFirst(), openAnim);
                  Color second = ColorUtil.applyOpacity(themeColors.getSecond(), openAnim);
                  float swatchHeight = 8.0F;
                  float swatchWidth = 8.0F;
                  float spacing = 3.0F;
                  float middleOfRect = rectHeight / 2.0F - swatchHeight / 2.0F;
                  float v = modeY + (middleOfRect + rectHeight * rectCount) * openAnim;
                  RoundedUtil.drawRound(modeX + boxWidth - (swatchWidth + 4.0F + (swatchWidth + spacing)), v, swatchWidth, swatchHeight, 2.25F, first);
                  RoundedUtil.drawRound(modeX + boxWidth - (swatchWidth + 4.0F), v, swatchWidth, swatchHeight, 2.25F, second);
               }

               rectCount++;
            }
         }

         this.countSize = 2.0F + (0.25F + rectCount * (rectHeight / (this.realHeight / this.normalCount))) * this.openAnimation.getOutput().floatValue();
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      float boxHeight = 18.0F;
      float boxY = this.y + this.realHeight / 2.0F - boxHeight / 2.0F + 3.0F;
      float boxX = this.x + 6.0F;
      float boxWidth = this.width - 10.0F;
      if (this.isClickable(boxY + boxHeight) && HoveringUtil.isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY) && button == 1) {
         this.opened = !this.opened;
      }

      if (this.opened) {
         float rectHeight = 15.0F;
         float rectCount = 0.0F;
         float modeY = boxY + boxHeight + 4.0F;
         float modeX = boxX - 1.0F;
         String current = this.setting.getValue();

         for (String mode : this.setting.getModes()) {
            if (!mode.equals(current)) {
               boolean hoveringMode = HoveringUtil.isHovering(modeX, modeY + rectCount * rectHeight, boxWidth, rectHeight, mouseX, mouseY);
               if (this.isClickable(modeY + rectCount * rectHeight + rectHeight) && hoveringMode && button == 0) {
                  this.setting.setValue(mode);
                  this.opened = false;
                  return;
               }

               rectCount++;
            }
         }
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }
}
