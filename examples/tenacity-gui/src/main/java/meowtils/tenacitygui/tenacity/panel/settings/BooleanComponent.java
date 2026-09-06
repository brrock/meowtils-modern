package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;

public class BooleanComponent extends SettingComponent {
   private final BooleanComponent.BoolAccess access;
   private final Animation toggleAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);

   public BooleanComponent(BooleanComponent.BoolAccess access) {
      this.access = access;
   }

   @Override
   public String getName() {
      return this.access.name();
   }

   @Override
   public void initGui() {
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      this.toggleAnimation.setDirection(this.access.get() ? Direction.FORWARDS : Direction.BACKWARDS);
      RenderUtil.resetColor();
      Fonts.tenacityFont16
         .drawString(
            this.access.name(),
            this.x + 5.0F,
            this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height),
            ColorUtil.applyOpacity(this.textColor, 0.5F + 0.5F * this.toggleAnimation.getOutput().floatValue())
         );
      float switchWidth = 17.0F;
      float switchHeight = 7.0F;
      float booleanX = this.x + this.width - (switchWidth + 5.5F);
      float booleanY = this.y + this.height / 2.0F - switchHeight / 2.0F;
      boolean hovering = HoveringUtil.isHovering(booleanX - 2.0F, booleanY - 2.0F, switchWidth + 4.0F, switchHeight + 4.0F, mouseX, mouseY);
      this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
      Color accentCircle = ColorUtil.applyOpacity(this.clientColors.getSecond(), this.alpha);
      Color rectColor = ColorUtil.interpolateColorC(this.settingRectColor.brighter().brighter(), accentCircle, this.toggleAnimation.getOutput().floatValue());
      rectColor = ColorUtil.interpolateColorC(rectColor, ColorUtil.brighter(rectColor, 0.8F), this.hoverAnimation.getOutput().floatValue());
      RenderUtil.resetColor();
      RoundedUtil.drawRound(booleanX, booleanY, switchWidth, switchHeight, 3.0F, rectColor);
      RenderUtil.resetColor();
      RoundedUtil.drawRound(
         this.x + this.width - (switchWidth + 4.0F) + (switchWidth - 8.0F) * this.toggleAnimation.getOutput().floatValue(),
         this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height) + 0.5F,
         5.0F,
         5.0F,
         2.0F,
         this.textColor
      );
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      float switchWidth = 17.0F;
      float switchHeight = 7.0F;
      float booleanX = this.x + this.width - (switchWidth + 5.5F);
      float booleanY = this.y + this.height / 2.0F - switchHeight / 2.0F;
      boolean hovering = HoveringUtil.isHovering(booleanX - 2.0F, booleanY - 2.0F, switchWidth + 4.0F, switchHeight + 4.0F, mouseX, mouseY);
      if (this.isClickable(booleanY + switchHeight) && hovering && button == 0) {
         this.access.toggle();
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }

   public interface BoolAccess {
      String name();

      boolean get();

      void toggle();
   }
}
