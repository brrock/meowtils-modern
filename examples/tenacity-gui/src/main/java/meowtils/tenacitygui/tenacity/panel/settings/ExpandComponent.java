package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import java.util.List;
import meowtils.tenacitygui.tenacity.adapter.ValueAdapter;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import wtf.tatp.meowtils.gui.values.ExpandValue;

public class ExpandComponent extends SettingComponent {
   private static final float ROW_HEIGHT = 16.0F;
   private final ExpandValue setting;
   private final List<SettingComponent> children;
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation openAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   public float realHeight;
   public float normalCount;
   private double childCount;

   public ExpandComponent(ExpandValue setting) {
      this.setting = setting;
      this.children = ValueAdapter.build(setting.getSubValues());
      this.normalCount = 1.0F;
   }

   @Override
   public String getName() {
      return this.setting.getName();
   }

   @Override
   public void initGui() {
      for (SettingComponent child : this.children) {
         child.initGui();
      }
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      if (this.setting.getState()) {
         for (SettingComponent child : this.children) {
            child.keyTyped(typedChar, keyCode);
         }
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      boolean opened = this.setting.getState();
      this.openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
      float openAnim = this.openAnimation.getOutput().floatValue();
      float headerHeight = 16.0F;
      float boxX = this.x + 5.0F;
      float boxWidth = this.width - 10.0F;
      float boxY = this.y + 1.0F;
      boolean hovering = HoveringUtil.isHovering(boxX, boxY, boxWidth, headerHeight - 2.0F, mouseX, mouseY);
      this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
      float bodyHeight = (float)(this.childCount * 16.0) * openAnim;
      Color outlineColor = ColorUtil.interpolateColorC(
         this.settingRectColor.brighter().brighter(), this.clientColors.getSecond(), 0.3F * this.hoverAnimation.getOutput().floatValue() + 0.7F * openAnim
      );
      RoundedUtil.drawRound(boxX, boxY, boxWidth, headerHeight - 2.0F + bodyHeight, 4.0F, outlineColor);
      RoundedUtil.drawRound(boxX + 1.0F, boxY + 1.0F, boxWidth - 2.0F, headerHeight - 4.0F + bodyHeight, 3.0F, this.settingRectColor.brighter());
      Fonts.tenacityFont16.drawString(this.setting.getName(), boxX + 5.0F, boxY + Fonts.tenacityFont16.getMiddleOfBox(headerHeight - 2.0F), this.textColor);
      RenderUtil.resetColor();
      float arrowX = boxX + boxWidth - 11.0F;
      float arrowY = boxY + Fonts.iconFont20.getMiddleOfBox(headerHeight - 2.0F) + 1.0F;
      RenderUtil.rotateStart(arrowX, arrowY, Fonts.iconFont20.getStringWidth("z"), Fonts.iconFont20.getHeight(), 180.0F * openAnim);
      Fonts.iconFont20.drawString("z", arrowX, arrowY, ColorUtil.applyOpacity(this.textColor, 0.5F));
      RenderUtil.rotateEnd();
      double count = 0.0;
      this.typing = false;
      if (opened || !this.openAnimation.isDone()) {
         for (SettingComponent child : this.children) {
            child.panelLimitY = this.panelLimitY;
            child.settingRectColor = this.settingRectColor.brighter();
            child.textColor = ColorUtil.applyOpacity(this.textColor, openAnim);
            child.clientColors = this.clientColors;
            child.alpha = this.alpha * openAnim;
            child.x = boxX;
            child.y = (float)(boxY + headerHeight + count * 16.0);
            child.width = boxWidth;
            ValueAdapter.applyRowHeight(child, 16.0F);
            child.height = 16.0F * child.countSize;
            child.drawScreen(mouseX, mouseY);
            if (child.typing) {
               this.typing = true;
            }

            count += child.countSize;
         }
      }

      this.childCount = count;
      this.realHeight = headerHeight + bodyHeight;
      this.countSize = 1.0F + (float)(count * openAnim);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      float headerHeight = 16.0F;
      float boxX = this.x + 5.0F;
      float boxWidth = this.width - 10.0F;
      float boxY = this.y + 1.0F;
      if (!this.isClickable(boxY + headerHeight)
         || !HoveringUtil.isHovering(boxX, boxY, boxWidth, headerHeight - 2.0F, mouseX, mouseY)
         || button != 0 && button != 1) {
         if (this.setting.getState() && this.openAnimation.finished(Direction.FORWARDS)) {
            for (SettingComponent child : this.children) {
               child.mouseClicked(mouseX, mouseY, button);
            }
         }
      } else {
         this.setting.setState(!this.setting.getState());
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
      if (this.setting.getState()) {
         for (SettingComponent child : this.children) {
            child.mouseReleased(mouseX, mouseY, state);
         }
      }
   }
}
