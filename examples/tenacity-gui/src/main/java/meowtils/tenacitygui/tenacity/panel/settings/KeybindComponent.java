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
import wtf.tatp.meowtils.extension.render.Keyboard;

public class KeybindComponent extends SettingComponent {
   private final KeybindComponent.BindAccess access;
   private boolean binding;
   private final Animation clickAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
   private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);

   public KeybindComponent(KeybindComponent.BindAccess access) {
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
      if (this.binding) {
         if (keyCode != 57 && keyCode != 1 && keyCode != 211) {
            this.access.set(keyCode);
         } else {
            this.access.set(0);
         }

         this.stopBinding();
      }
   }

   private void stopBinding() {
      this.binding = false;
      this.typing = false;
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      this.clickAnimation.setDirection(this.binding ? Direction.FORWARDS : Direction.BACKWARDS);
      this.typing = this.binding;
      String bind = this.binding ? "..." : keyName(this.access.get());
      float fullTextWidth = Fonts.tenacityFont16.getStringWidth("Bind: §l" + bind);
      float startX = this.x + this.width / 2.0F - fullTextWidth / 2.0F;
      float startY = this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height);
      boolean hovering = HoveringUtil.isHovering(startX - 3.0F, startY - 2.0F, fullTextWidth + 6.0F, Fonts.tenacityFont16.getHeight() + 4, mouseX, mouseY);
      this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
      Color rectColor = ColorUtil.brighter(this.settingRectColor, 0.7F - 0.25F * this.hoverAnimation.getOutput().floatValue());
      RoundedUtil.drawRound(startX - 3.0F, startY - 2.0F, fullTextWidth + 6.0F, Fonts.tenacityFont16.getHeight() + 4, 4.0F, rectColor);
      Fonts.tenacityFont16
         .drawCenteredString("Bind: §l" + bind, this.x + this.width / 2.0F, this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height), this.textColor);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      String bind = keyName(this.access.get());
      String text = "§fBind: §r" + bind;
      float textWidth = Fonts.tenacityFont18.getStringWidth(text);
      float startX = this.x + this.width / 2.0F - textWidth / 2.0F;
      float startY = this.y + Fonts.tenacityFont18.getMiddleOfBox(this.height);
      float rectHeight = Fonts.tenacityFont18.getHeight() + 4;
      boolean hovering = HoveringUtil.isHovering(startX - 3.0F, startY - 2.0F, textWidth + 6.0F, Fonts.tenacityFont18.getHeight() + 4, mouseX, mouseY);
      if (this.isClickable(startY + rectHeight) && hovering && button == 0) {
         this.binding = true;
         this.typing = true;
      } else if (this.binding) {
         this.stopBinding();
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }

   static String keyName(int code) {
      if (code == 0) {
         return "None";
      } else {
         String name = Keyboard.getKeyName(code);
         return name == null ? "None" : name;
      }
   }

   public interface BindAccess {
      String name();

      int get();

      void set(int var1);
   }
}
