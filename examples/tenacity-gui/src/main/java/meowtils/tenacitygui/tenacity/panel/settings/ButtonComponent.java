package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import wtf.tatp.meowtils.gui.values.ButtonValue;

public class ButtonComponent extends SettingComponent {
   private final ButtonValue setting;
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation clickAnimation = new DecelerateAnimation(300, 1.0, Direction.BACKWARDS);

   public ButtonComponent(ButtonValue setting) {
      this.setting = setting;
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
      String name = this.setting.getName();
      float textWidth = Fonts.tenacityFont16.getStringWidth(name);
      float rectWidth = Math.min(this.width - 10.0F, textWidth + 12.0F);
      float rectHeight = Fonts.tenacityFont16.getHeight() + 6;
      float rectX = this.x + this.width / 2.0F - rectWidth / 2.0F;
      float rectY = this.y + this.height / 2.0F - rectHeight / 2.0F;
      boolean hovering = HoveringUtil.isHovering(rectX, rectY, rectWidth, rectHeight, mouseX, mouseY);
      this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
      if (this.clickAnimation.finished(Direction.FORWARDS)) {
         this.clickAnimation.setDirection(Direction.BACKWARDS);
      }

      Color base = ColorUtil.brighter(this.settingRectColor, 0.7F - 0.25F * this.hoverAnimation.getOutput().floatValue());
      Color accent = ColorUtil.applyOpacity(this.clientColors.getSecond(), this.alpha);
      Color rectColor = ColorUtil.interpolateColorC(
         base, accent, 0.35F * this.hoverAnimation.getOutput().floatValue() + 0.65F * this.clickAnimation.getOutput().floatValue()
      );
      RoundedUtil.drawRound(rectX, rectY, rectWidth, rectHeight, 4.0F, rectColor);
      Fonts.tenacityFont16.drawCenteredString(name, this.x + this.width / 2.0F, this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height), this.textColor);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      float textWidth = Fonts.tenacityFont16.getStringWidth(this.setting.getName());
      float rectWidth = Math.min(this.width - 10.0F, textWidth + 12.0F);
      float rectHeight = Fonts.tenacityFont16.getHeight() + 6;
      float rectX = this.x + this.width / 2.0F - rectWidth / 2.0F;
      float rectY = this.y + this.height / 2.0F - rectHeight / 2.0F;
      if (this.isClickable(rectY + rectHeight) && HoveringUtil.isHovering(rectX, rectY, rectWidth, rectHeight, mouseX, mouseY) && button == 0) {
         this.clickAnimation.setDirection(Direction.FORWARDS);
         this.setting.click();
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }
}
