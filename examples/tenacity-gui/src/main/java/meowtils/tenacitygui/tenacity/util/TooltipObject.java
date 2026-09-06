package meowtils.tenacitygui.tenacity.util;

import meowtils.tenacitygui.tenacity.Screen;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;

public class TooltipObject implements Screen {
   private boolean hovering = false;
   private boolean round = true;
   private final Animation fadeInAnimation = new DecelerateAnimation(250, 1.0).setDirection(Direction.BACKWARDS);
   private String tooltip;
   private String additionalInformation;
   private float width = 150.0F;
   private float height = 40.0F;

   public TooltipObject(String tooltip) {
      this.tooltip = tooltip;
   }

   public TooltipObject() {
   }

   @Override
   public void initGui() {
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      this.fadeInAnimation.setDirection(this.hovering ? Direction.FORWARDS : Direction.BACKWARDS);
      float x = mouseX - 2;
      float y = mouseY + 13;
      float fadeAnim = this.fadeInAnimation.getOutput().floatValue();
      if (this.tooltip != null && !this.fadeInAnimation.finished(Direction.BACKWARDS)) {
         if (this.tooltip.contains("\n")) {
            RenderUtil.scissorStart(x - 1.5F, y - 1.5F, (this.width + 4.0F) * fadeAnim, this.height + 4.0F);
            RoundedUtil.drawRound(x - 0.75F, y - 0.75F, this.width + 1.5F, this.height + 1.5F, 3.0F, ColorUtil.tripleColor(45, fadeAnim));
            RoundedUtil.drawRound(x, y, this.width, this.height, 2.5F, ColorUtil.applyOpacity(ColorUtil.tripleColor(15), fadeAnim));
            MutablePair<Float, Float> whPair = Fonts.tenacityFont14
               .drawNewLineText(this.tooltip, x + 2.0F, y + 2.0F, ColorUtil.applyOpacity(-1, fadeAnim), 3.0F);
            float additionalHeight = 0.0F;
            if (this.additionalInformation != null) {
               additionalHeight = Fonts.tenacityFont14
                  .drawWrappedText(this.additionalInformation, x + 2.0F, y + 1.5F + whPair.getSecond(), ColorUtil.applyOpacity(-1, fadeAnim), this.width, 3.0F);
            }

            RenderUtil.scissorEnd();
            if (this.additionalInformation != null) {
               this.width = Math.max(150.0F, whPair.getFirst() + 4.0F);
            } else {
               this.width = whPair.getFirst() + 4.0F;
            }

            this.height = whPair.getSecond() + additionalHeight;
         } else {
            this.width = Fonts.tenacityFont14.getStringWidth(this.tooltip) + 4.0F;
            this.height = Fonts.tenacityFont14.getHeight() + 2;
            RenderUtil.scissorStart(x - 1.5F, y - 1.5F, (this.width + 4.0F) * fadeAnim, this.height + 4.0F);
            if (this.round) {
               RoundedUtil.drawRound(x - 0.75F, y - 0.75F, this.width + 1.5F, this.height + 1.5F, 3.0F, ColorUtil.tripleColor(45, fadeAnim));
               RoundedUtil.drawRound(x, y, this.width, this.height, 2.5F, ColorUtil.applyOpacity(ColorUtil.tripleColor(15), fadeAnim));
            } else {
               RenderUtil.drawBorderedRect(
                  x, y, this.width, this.height, 1.0F, ColorUtil.tripleColor(15, fadeAnim).getRGB(), ColorUtil.tripleColor(45, fadeAnim).getRGB()
               );
            }

            Fonts.tenacityFont14
               .drawCenteredString(
                  this.tooltip, x + this.width / 2.0F, y + Fonts.tenacityFont14.getMiddleOfBox(this.height), ColorUtil.applyOpacity(-1, fadeAnim)
               );
            RenderUtil.scissorEnd();
         }
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }

   public void setTip(String tooltip) {
      this.tooltip = tooltip;
   }

   public void setAdditionalInformation(String additionalInformation) {
      this.additionalInformation = additionalInformation;
   }

   public boolean isHovering() {
      return this.hovering;
   }

   public void setHovering(boolean hovering) {
      this.hovering = hovering;
   }

   public void setRound(boolean round) {
      this.round = round;
   }

   public Animation getFadeInAnimation() {
      return this.fadeInAnimation;
   }
}
