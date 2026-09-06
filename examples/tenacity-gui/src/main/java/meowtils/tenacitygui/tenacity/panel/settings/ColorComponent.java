package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.GLUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.render.TextureUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.MathUtils;
import meowtils.tenacitygui.tenacity.util.Pair;
import meowtils.tenacitygui.tenacity.util.TextField;
import wtf.tatp.meowtils.gui.ColorLink;

public class ColorComponent extends SettingComponent {
   private final String name;
   private final ColorLink link;
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation openAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Pair<Animation, Animation> errorAnimations = Pair.of(
      new DecelerateAnimation(1000, 1.0, Direction.BACKWARDS), new DecelerateAnimation(250, 1.0, Direction.BACKWARDS)
   );
   public float realHeight;
   public float openedHeight;
   private boolean opened;
   private TextField hexField;
   private boolean draggingPicker;
   private boolean draggingHue;
   private static final String HEX_LETTERS = "abcdefABCDEF0123456789";

   public ColorComponent(String name, ColorLink link) {
      this.name = name;
      this.link = link;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public void initGui() {
   }

   private TextField hexField() {
      if (this.hexField == null) {
         this.hexField = new TextField(Fonts.tenacityFont16);
      }

      return this.hexField;
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      char c = typedChar;
      if ("abcdefABCDEF0123456789".indexOf(typedChar) < 0) {
         c = 167;
      }

      this.hexField().keyTyped(c, keyCode);
   }

   private Color currentColor() {
      return new Color(this.link.getRGB());
   }

   private String hexCode() {
      Color color = this.currentColor();
      return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      TextField hexField = this.hexField();
      this.openAnimation.setDirection(this.opened ? Direction.FORWARDS : Direction.BACKWARDS);
      Fonts.tenacityFont16.drawString(this.name, this.x + 5.0F, this.y + Fonts.tenacityFont16.getMiddleOfBox(this.realHeight), this.textColor);
      float spacing = 4.0F;
      float colorHeight = 6.5F;
      float colorWidth = 30.0F;
      float colorX = this.x + this.width - (colorWidth + spacing);
      float colorY = this.y + this.realHeight / 2.0F - colorHeight / 2.0F;
      float colorRadius = 3.0F;
      float openAnim = this.openAnimation.getOutput().floatValue();
      float newColorY = this.y + this.realHeight - 1.0F;
      float newColorHeight = 5.0F
         + (!this.openAnimation.finished(Direction.FORWARDS) && this.openAnimation.isDone() ? 0.0F : 5.0F * this.hoverAnimation.getOutput().floatValue());
      colorX = MathUtils.interpolateFloat(colorX, this.x + 6.0F, openAnim);
      colorY = MathUtils.interpolateFloat(colorY, newColorY, openAnim);
      colorWidth = MathUtils.interpolateFloat(colorWidth, this.width - 12.0F, openAnim);
      colorHeight = MathUtils.interpolateFloat(colorHeight, newColorHeight, openAnim);
      colorRadius = MathUtils.interpolateFloat(colorRadius, 2.0F, openAnim);
      boolean hovered = HoveringUtil.isHovering(colorX - 4.0F, colorY - 4.0F, colorWidth + 8.0F, colorHeight + 8.0F, mouseX, mouseY);
      this.hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
      Color actualColor = ColorUtil.applyOpacity(this.currentColor(), this.alpha);
      RoundedUtil.drawRound(
         colorX,
         colorY,
         colorWidth,
         colorHeight,
         colorRadius,
         ColorUtil.interpolateColorC(actualColor, actualColor.darker(), this.hoverAnimation.getOutput().floatValue())
      );
      String text = "Right click for picker";
      Fonts.tenacityFont14
         .drawCenteredStringWithShadow(
            text,
            colorX + colorWidth / 2.0F,
            colorY + Fonts.tenacityFont14.getMiddleOfBox(colorHeight),
            ColorUtil.applyOpacity(-1, this.hoverAnimation.getOutput().floatValue() * (openAnim * openAnim))
         );
      if (this.opened || !this.openAnimation.isDone()) {
         float[] hsb = new float[]{this.link.getHue(), this.link.getSaturation(), this.link.getBrightness()};
         float gradientX = this.x + 6.0F;
         float gradientY = newColorY + colorHeight + 4.0F;
         float gradientWidth = this.width - 12.0F;
         float gradientHeight = 10.0F + 55.0F * openAnim;
         float radius = 2.0F;
         float colorAlpha = this.alpha * openAnim;
         if (this.draggingHue) {
            hsb[0] = Math.min(1.0F, Math.max(0.0F, (mouseX - gradientX) / gradientWidth));
            this.link.apply(hsb[0], hsb[1], hsb[2]);
         }

         if (this.draggingPicker) {
            hsb[2] = Math.min(1.0F, Math.max(0.0F, 1.0F - (mouseY - gradientY) / gradientHeight));
            hsb[1] = Math.min(1.0F, Math.max(0.0F, (mouseX - gradientX) / gradientWidth));
            this.link.apply(hsb[0], hsb[1], hsb[2]);
         }

         Color firstColor = ColorUtil.applyOpacity(Color.getHSBColor(hsb[0], 1.0F, 1.0F), colorAlpha);
         RoundedUtil.drawRound(gradientX, gradientY, gradientWidth, gradientHeight, radius, ColorUtil.applyOpacity(firstColor, colorAlpha));
         Color secondColor = Color.getHSBColor(hsb[0], 0.0F, 1.0F);
         RoundedUtil.drawGradientHorizontal(
            gradientX,
            gradientY,
            gradientWidth,
            gradientHeight,
            radius + 0.5F,
            ColorUtil.applyOpacity(secondColor, colorAlpha),
            ColorUtil.applyOpacity(secondColor, 0.0F)
         );
         Color thirdColor = Color.getHSBColor(hsb[0], 1.0F, 0.0F);
         RoundedUtil.drawGradientVertical(
            gradientX,
            gradientY,
            gradientWidth,
            gradientHeight,
            radius,
            ColorUtil.applyOpacity(thirdColor, 0.0F),
            ColorUtil.applyOpacity(thirdColor, colorAlpha)
         );
         float pickerY = gradientY - 2.0F + gradientHeight * (1.0F - hsb[2]);
         float pickerX = gradientX + (gradientWidth * hsb[1] - 1.0F);
         pickerY = Math.max(Math.min(gradientY + gradientHeight - 2.0F, pickerY), gradientY - 2.0F);
         pickerX = Math.max(Math.min(gradientX + gradientWidth - 2.0F, pickerX), gradientX - 2.0F);
         Color whiteColor = ColorUtil.applyOpacity(Color.WHITE, colorAlpha);
         RenderUtil.color(whiteColor.getRGB());
         GLUtil.startBlend();
         TextureUtil.drawImage("colorpicker2", pickerX, pickerY, 4.0F, 4.0F);
         GLUtil.endBlend();
         float hueY = gradientY + gradientHeight + 5.0F;
         float hueHeight = 4.0F;
         RenderUtil.resetColor();
         RoundedUtil.drawRoundTextured(TextureUtil.get("hue"), gradientX, hueY, gradientWidth, hueHeight, 1.5F, colorAlpha);
         float sliderSize = 6.5F;
         float sliderX = gradientX + gradientWidth * hsb[0] - sliderSize / 2.0F;
         RoundedUtil.drawRound(sliderX, hueY + (hueHeight / 2.0F - sliderSize / 2.0F), sliderSize, sliderSize, sliderSize / 2.0F - 0.5F, whiteColor);
         float miniSize = 4.25F;
         float movement = sliderSize / 2.0F - miniSize / 2.0F;
         RoundedUtil.drawRound(sliderX + movement, hueY + (hueHeight / 2.0F - miniSize / 2.0F), miniSize, miniSize, miniSize / 2.0F - 0.5F, firstColor);
         Animation error2Anim = this.errorAnimations.getSecond();
         float newYVal = hueY + hueHeight + 4.0F + 5.0F * error2Anim.getOutput().floatValue();
         float heightLeft = this.height - (newYVal - this.y);
         Fonts.tenacityFont16.drawString("Hex", gradientX, newYVal + Fonts.tenacityFont16.getMiddleOfBox(heightLeft), whiteColor);
         hexField.setWidth(50.0F);
         hexField.setHeight(12.0F);
         hexField.setXPosition(gradientX + (gradientWidth - hexField.getWidth() - 5.0F));
         hexField.setYPosition(newYVal + heightLeft / 2.0F - hexField.getHeight() / 2.0F);
         Color settingColor = ColorUtil.applyOpacity(this.settingRectColor.brighter(), openAnim);
         hexField.setOutline(settingColor.brighter().brighter());
         hexField.setFill(settingColor);
         hexField.setTextAlpha(colorAlpha);
         hexField.setMaxStringLength(6);
         if (!hexField.isFocused()) {
            hexField.setText(this.hexCode());
            error2Anim.setDirection(Direction.BACKWARDS);
         } else {
            try {
               Color textFieldColor = Color.decode("#" + hexField.getText());
               float[] typed = Color.RGBtoHSB(textFieldColor.getRed(), textFieldColor.getGreen(), textFieldColor.getBlue(), null);
               this.link.apply(typed[0], typed[1], typed[2]);
               error2Anim.setDirection(Direction.BACKWARDS);
            } catch (Exception var41) {
               Animation blinkAnimation = this.errorAnimations.getFirst();
               error2Anim.setDirection(Direction.FORWARDS);
               if (blinkAnimation.isDone()) {
                  blinkAnimation.changeDirection();
               }

               Fonts.tenacityFont14
                  .drawString(
                     "Invalid Hex Code",
                     hexField.getXPosition() - 0.5F,
                     newYVal - (Fonts.tenacityFont14.getHeight() - 0.5F),
                     ColorUtil.applyOpacity(Color.RED, blinkAnimation.getOutput().floatValue())
                  );
            }
         }

         hexField.drawTextBox();
      }

      this.typing = hexField.isFocused();
      Animation errorAnimation = this.errorAnimations.getSecond();
      this.openedHeight = this.realHeight * (1.0F + 6.75F * openAnim);
      this.countSize = 1.0F + 6.75F * openAnim + errorAnimation.getOutput().floatValue() * 0.25F;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      TextField hexField = this.hexField();
      float spacing = 4.0F;
      float colorHeight = 6.5F;
      float colorWidth = 30.0F;
      float colorX = this.x + this.width - (colorWidth + spacing);
      float colorY = this.y + this.realHeight / 2.0F - colorHeight / 2.0F;
      float newColorY = this.y + this.realHeight - 1.0F;
      float openAnim = this.openAnimation.getOutput().floatValue();
      colorX = MathUtils.interpolateFloat(colorX, this.x + 6.0F, openAnim);
      colorY = MathUtils.interpolateFloat(colorY, newColorY, openAnim);
      colorWidth = MathUtils.interpolateFloat(colorWidth, this.width - 12.0F, openAnim);
      colorHeight = MathUtils.interpolateFloat(colorHeight, 5.0F, openAnim);
      boolean hovered = this.isClickable(colorY + colorHeight)
         && HoveringUtil.isHovering(colorX - 4.0F, colorY - 4.0F, colorWidth + 8.0F, colorHeight + 8.0F, mouseX, mouseY);
      if (hovered && button == 1) {
         this.opened = !this.opened;
         hexField.mouseClicked(mouseX, mouseY, button);
      }

      if (this.opened) {
         float gradientX = this.x + 6.0F;
         float gradientY = newColorY + colorHeight + 4.0F;
         float gradientWidth = this.width - 12.0F;
         float gradientHeight = 10.0F + 55.0F * openAnim;
         if (button == 0) {
            float hueY = gradientY + gradientHeight + 5.0F;
            if (this.isClickable(hueY + 4.0F) && HoveringUtil.isHovering(gradientX, hueY, gradientWidth, 4.0F, mouseX, mouseY)) {
               this.draggingHue = true;
            }

            if (this.isClickable(gradientY + gradientHeight) && HoveringUtil.isHovering(gradientX, gradientY, gradientWidth, gradientHeight, mouseX, mouseY)) {
               this.draggingPicker = true;
            }
         }

         hexField.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
      this.draggingHue = false;
      this.draggingPicker = false;
   }
}
