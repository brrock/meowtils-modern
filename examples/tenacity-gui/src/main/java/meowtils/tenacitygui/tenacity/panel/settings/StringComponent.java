package meowtils.tenacitygui.tenacity.panel.settings;

import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.util.TextField;
import wtf.tatp.meowtils.gui.values.TextValue;

public class StringComponent extends SettingComponent {
   private final TextValue setting;
   private TextField textField;
   boolean setDefaultText = false;

   public StringComponent(TextValue setting) {
      this.setting = setting;
   }

   @Override
   public String getName() {
      return this.setting.getName();
   }

   @Override
   public void initGui() {
      this.setDefaultText = false;
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      this.field().keyTyped(typedChar, keyCode);
   }

   private TextField field() {
      if (this.textField == null) {
         this.textField = new TextField(Fonts.tenacityFont16);
      }

      return this.textField;
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      TextField textField = this.field();
      float boxX = this.x + 6.0F;
      float boxY = this.y + 12.0F;
      float boxWidth = this.width - 12.0F;
      float boxHeight = this.height - 16.0F;
      if (!this.setDefaultText) {
         textField.setText(this.setting.get());
         textField.setCursorPositionZero();
         this.setDefaultText = true;
      }

      this.setting.set(textField.getText());
      textField.setBackgroundText(this.setting.getDescription());
      Fonts.tenacityFont14.drawString(this.setting.getName(), boxX, this.y + 3.0F, this.textColor);
      textField.setXPosition(boxX);
      textField.setYPosition(boxY);
      textField.setWidth(boxWidth);
      textField.setHeight(boxHeight);
      textField.setOutline(this.settingRectColor.brighter().brighter().brighter());
      textField.setFill(this.settingRectColor.brighter());
      textField.drawTextBox();
      this.typing = textField.isFocused();
      this.countSize = 2.0F;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      this.field().mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }
}
