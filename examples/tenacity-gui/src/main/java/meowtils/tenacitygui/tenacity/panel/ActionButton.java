package meowtils.tenacitygui.tenacity.panel;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.Screen;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;

public class ActionButton implements Screen {
   private final String name;
   private final Runnable clickAction;
   public float x;
   public float y;
   public float width;
   public float height;
   public float alpha = 1.0F;
   private Color color = ColorUtil.tripleColor(55);
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);

   public ActionButton(String name, Runnable clickAction) {
      this.name = name;
      this.clickAction = clickAction;
   }

   public float preferredWidth(float horizontalPadding) {
      return Fonts.tenacityFont18.getStringWidth(this.name) + horizontalPadding * 2.0F;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   @Override
   public void initGui() {
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      boolean hovering = HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY);
      this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
      Color rectColor = ColorUtil.interpolateColorC(this.color, this.color.brighter(), this.hoverAnimation.getOutput().floatValue());
      RenderUtil.resetColor();
      RoundedUtil.drawRound(this.x, this.y, this.width, this.height, 5.0F, ColorUtil.applyOpacity(rectColor, this.alpha));
      RenderUtil.resetColor();
      Fonts.tenacityFont18
         .drawCenteredString(
            this.name, this.x + this.width / 2.0F, this.y + Fonts.tenacityFont18.getMiddleOfBox(this.height), ColorUtil.applyOpacity(-1, this.alpha)
         );
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (button == 0 && HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY) && this.clickAction != null) {
         this.clickAction.run();
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }
}
