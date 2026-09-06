package meowtils.tenacitygui.tenacity.panel.settings;

import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.ContinualAnimation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.MathUtils;
import meowtils.tenacitygui.tenacity.util.Pair;
import wtf.tatp.meowtils.extension.render.Keyboard;

public class NumberComponent extends SettingComponent {
   private final NumberComponent.NumAccess access;
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Pair<Animation, Animation> textAnimations = Pair.of(new DecelerateAnimation(250, 1.0), new DecelerateAnimation(250, 1.0, Direction.BACKWARDS));
   private boolean dragging;
   private final ContinualAnimation animationWidth = new ContinualAnimation();
   public float clickCountAdd = 0.0F;
   private boolean selected;

   public NumberComponent(NumberComponent.NumAccess access) {
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
      if (this.selected) {
         Keyboard.enableRepeatEvents(true);
         double increment = this.access.increment();
         switch (keyCode) {
            case 203:
               this.access.set(this.access.get() - increment);
               break;
            case 205:
               this.access.set(this.access.get() + increment);
         }
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      String value = String.valueOf(MathUtils.round(this.access.get(), 2));
      value = value.contains(".") ? value.replaceAll("0*$", "").replaceAll("\\.$", "") : value;
      float sliderX = this.x + 5.0F;
      float sliderWidth = this.width - 10.0F;
      float sliderY = this.y + 13.0F;
      float sliderHeight = 3.0F;
      this.textAnimations.getFirst().setDirection(this.dragging ? Direction.BACKWARDS : Direction.FORWARDS);
      this.textAnimations.getSecond().setDirection(this.selected && !this.dragging ? Direction.FORWARDS : Direction.BACKWARDS);
      boolean hovering = HoveringUtil.isHovering(sliderX, sliderY - 2.0F, sliderWidth, sliderHeight + 4.0F, mouseX, mouseY);
      this.hoverAnimation.setDirection(!hovering && !this.dragging ? Direction.BACKWARDS : Direction.FORWARDS);
      float firstTextAnim = this.textAnimations.getFirst().getOutput().floatValue();
      float funnyWidth = Fonts.tenacityFont16.getStringWidth(this.access.name()) - Fonts.tenacityFont16.getStringWidth(": " + value);
      Fonts.tenacityFont16
         .drawString(
            ": §l" + value,
            sliderX + funnyWidth + Fonts.tenacityFont16.getStringWidth(": " + value) * firstTextAnim,
            this.y + 2.0F,
            ColorUtil.applyOpacity(this.textColor, firstTextAnim)
         );
      String text = "You can use arrow keys";
      Fonts.tenacityFont14
         .drawCenteredString(
            text,
            this.x + this.width / 2.0F,
            sliderY + sliderHeight + 4.5F,
            ColorUtil.applyOpacity(-1, this.textAnimations.getSecond().getOutput().floatValue() * 0.25F)
         );
      Fonts.tenacityFont16.drawString(this.access.name(), sliderX, this.y + 2.0F, this.textColor);
      RoundedUtil.drawRound(
         sliderX,
         sliderY,
         sliderWidth,
         sliderHeight,
         1.5F,
         ColorUtil.brighter(this.settingRectColor, 0.7F - 0.2F * this.hoverAnimation.getOutput().floatValue())
      );
      double currentValue = this.access.get();
      if (this.dragging) {
         float percent = Math.min(1.0F, Math.max(0.0F, (mouseX - sliderX) / sliderWidth));
         double newValue = MathUtils.interpolate(this.access.min(), this.access.max(), percent);
         this.access.set(newValue);
      }

      float widthPercentage = (float)((currentValue - this.access.min()) / (this.access.max() - this.access.min()));
      this.animationWidth.animate(sliderWidth * widthPercentage, 20);
      float animatedWidth = this.animationWidth.getOutput();
      RoundedUtil.drawRound(sliderX, sliderY, animatedWidth, sliderHeight, 1.5F, this.clientColors.getSecond());
      float size = 7.0F;
      RoundedUtil.drawRound(sliderX + animatedWidth - size / 2.0F, sliderY - (size / 4.0F + 0.5F), size, size, size / 2.0F - 0.5F, this.settingRectColor);
      size = 5.0F;
      RoundedUtil.drawRound(sliderX + animatedWidth - size / 2.0F, sliderY - size / 4.0F, size, size, size / 2.0F - 0.5F, this.textColor);
      float secondTextAnim = 1.0F - this.textAnimations.getFirst().getOutput().floatValue();
      float rectWidth = Fonts.tenacityFont14.getStringWidth("§l" + value) + 4.0F;
      float rectX = Math.max(this.x, 2.0F + (sliderX + animatedWidth - size / 2.0F) - rectWidth / 2.0F);
      float rectY = sliderY + sliderHeight + 4.0F;
      float rectHeight = Fonts.tenacityFont14.getHeight() + 2;
      RoundedUtil.drawRound(rectX, rectY, rectWidth, rectHeight, 3.0F, ColorUtil.applyOpacity(this.settingRectColor.brighter(), secondTextAnim));
      Fonts.tenacityFont14
         .drawString(
            "§l" + value, rectX + 2.0F, rectY + Fonts.tenacityFont14.getMiddleOfBox(rectHeight), ColorUtil.applyOpacity(this.textColor, secondTextAnim)
         );
      this.clickCountAdd = 0.3F * secondTextAnim + 0.3F * this.textAnimations.getSecond().getOutput().floatValue();
      this.countSize = (float)(1.5 + this.clickCountAdd);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      float sliderX = this.x + 5.0F;
      float sliderWidth = this.width - 10.0F;
      float sliderY = this.y + this.height / 2.0F + 2.0F;
      float sliderHeight = 3.0F;
      if (!HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY)) {
         this.selected = false;
      }

      if (this.isClickable(sliderY + sliderHeight)
         && HoveringUtil.isHovering(sliderX, sliderY - 2.0F, sliderWidth, sliderHeight + 4.0F, mouseX, mouseY)
         && button == 0) {
         this.selected = true;
         this.dragging = true;
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
      if (this.dragging) {
         this.dragging = false;
      }
   }

   public interface NumAccess {
      String name();

      double get();

      void set(double var1);

      double min();

      double max();

      double increment();
   }
}
