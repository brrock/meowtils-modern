package meowtils.tenacitygui.tenacity.util;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.CustomFont;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;

import wtf.tatp.meowtils.extension.render.GuiScreen;
import wtf.tatp.meowtils.extension.render.GlStateManager;
import wtf.tatp.meowtils.extension.render.ChatAllowedCharacters;
import wtf.tatp.meowtils.extension.render.MathHelper;
import wtf.tatp.meowtils.extension.render.Keyboard;

public class TextField  {
   public CustomFont font;
   private float xPosition;
   private float yPosition;
   private float radius = 2.0F;
   private float alpha = 1.0F;
   private float width;
   private float height;
   private float textAlpha = 1.0F;
   private Color outline = Color.WHITE;
   private Color fill = ColorUtil.tripleColor(32);
   private Color focusedTextColor = new Color(224, 224, 224);
   private Color unfocusedTextColor = new Color(130, 130, 130);
   private String text = "";
   private String backgroundText;
   private int maxStringLength = 32;
   private boolean drawingBackground = true;
   private boolean canLoseFocus = true;
   private boolean isFocused;
   private int lineScrollOffset;
   private int cursorPosition;
   private int selectionEnd;
   private final Animation textColor = new DecelerateAnimation(250, 1.0);
   private final Animation cursorBlinkAnimation = new DecelerateAnimation(750, 1.0);
   private final TimerUtil timerUtil = new TimerUtil();
   private boolean visible = true;

   public TextField(CustomFont font) {
      this.font = font;
   }

   public TextField(CustomFont font, float x, float y, float par5Width, float par6Height) {
      this.font = font;
      this.xPosition = x;
      this.yPosition = y;
      this.width = par5Width;
      this.height = par6Height;
   }

   public void setText(String text) {
      if (text.length() > this.maxStringLength) {
         this.text = text.substring(0, this.maxStringLength);
      } else {
         this.text = text;
      }

      this.setCursorPositionZero();
   }

   public String getText() {
      return this.text;
   }

   public String getSelectedText() {
      int i = Math.min(this.cursorPosition, this.selectionEnd);
      int j = Math.max(this.cursorPosition, this.selectionEnd);
      return this.text.substring(i, j);
   }

   public void writeText(String text) {
      String s = "";
      String s1 = ChatAllowedCharacters.func_71565_a(text);
      int min = Math.min(this.cursorPosition, this.selectionEnd);
      int max = Math.max(this.cursorPosition, this.selectionEnd);
      int len = this.maxStringLength - this.text.length() - (min - max);
      if (this.text.length() > 0) {
         s = s + this.text.substring(0, min);
      }

      int l;
      if (len < s1.length()) {
         s = s + s1.substring(0, len);
         l = len;
      } else {
         s = s + s1;
         l = s1.length();
      }

      if (this.text.length() > 0 && max < this.text.length()) {
         s = s + this.text.substring(max);
      }

      this.text = s;
      this.moveCursorBy(min - this.selectionEnd + l);
   }

   public void deleteWords(int num) {
      if (this.text.length() != 0) {
         if (this.selectionEnd != this.cursorPosition) {
            this.writeText("");
         } else {
            this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
         }
      }
   }

   public void deleteFromCursor(int num) {
      if (this.text.length() != 0) {
         if (this.selectionEnd != this.cursorPosition) {
            this.writeText("");
         } else {
            boolean negative = num < 0;
            int i = negative ? this.cursorPosition + num : this.cursorPosition;
            int j = negative ? this.cursorPosition : this.cursorPosition + num;
            String s = "";
            if (i >= 0) {
               s = this.text.substring(0, i);
            }

            if (j < this.text.length()) {
               s = s + this.text.substring(j);
            }

            this.text = s;
            if (negative) {
               this.moveCursorBy(num);
            }
         }
      }
   }

   public int getNthWordFromCursor(int n) {
      return this.getNthWordFromPos(n, this.getCursorPosition());
   }

   public int getNthWordFromPos(int n, int pos) {
      return this.func_146197_a(n, pos);
   }

   public int func_146197_a(int n, int pos) {
      int i = pos;
      boolean negative = n < 0;
      int j = Math.abs(n);

      for (int k = 0; k < j; k++) {
         if (!negative) {
            int l = this.text.length();
            i = this.text.indexOf(32, i);
            if (i == -1) {
               i = l;
            } else {
               while (i < l && this.text.charAt(i) == ' ') {
                  i++;
               }
            }
         } else {
            while (i > 0 && this.text.charAt(i - 1) == ' ') {
               i--;
            }

            while (i > 0 && this.text.charAt(i - 1) != ' ') {
               i--;
            }
         }
      }

      return i;
   }

   public void moveCursorBy(int p_146182_1_) {
      this.setCursorPosition(this.selectionEnd + p_146182_1_);
   }

   public void setCursorPosition(int p_146190_1_) {
      this.cursorPosition = p_146190_1_;
      int i = this.text.length();
      this.cursorPosition = MathHelper.func_76125_a(this.cursorPosition, 0, i);
      this.setSelectionPos(this.cursorPosition);
   }

   public void setCursorPositionZero() {
      this.setCursorPosition(0);
   }

   public void setCursorPositionEnd() {
      this.setCursorPosition(this.text.length());
   }

   public boolean keyTyped(char cha, int keyCode) {
      if (!this.isFocused) {
         return false;
      } else {
         this.timerUtil.reset();
         if (GuiScreen.func_175278_g(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
         } else if (GuiScreen.func_175280_f(keyCode)) {
            GuiScreen.func_146275_d(this.getSelectedText());
            return true;
         } else if (GuiScreen.func_175279_e(keyCode)) {
            this.writeText(GuiScreen.func_146277_j());
            return true;
         } else if (GuiScreen.func_175277_d(keyCode)) {
            GuiScreen.func_146275_d(this.getSelectedText());
            this.writeText("");
            return true;
         } else {
            switch (keyCode) {
               case 14:
                  if (GuiScreen.func_146271_m()) {
                     this.deleteWords(-1);
                  } else {
                     this.deleteFromCursor(-1);
                  }

                  return true;
               case 199:
                  if (GuiScreen.func_146272_n()) {
                     this.setSelectionPos(0);
                  } else {
                     this.setCursorPositionZero();
                  }

                  return true;
               case 203:
                  if (GuiScreen.func_146272_n()) {
                     if (GuiScreen.func_146271_m()) {
                        this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                     } else {
                        this.setSelectionPos(this.getSelectionEnd() - 1);
                     }
                  } else if (GuiScreen.func_146271_m()) {
                     this.setCursorPosition(this.getNthWordFromCursor(-1));
                  } else {
                     this.moveCursorBy(-1);
                  }

                  return true;
               case 205:
                  if (GuiScreen.func_146272_n()) {
                     if (GuiScreen.func_146271_m()) {
                        this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                     } else {
                        this.setSelectionPos(this.getSelectionEnd() + 1);
                     }
                  } else if (GuiScreen.func_146271_m()) {
                     this.setCursorPosition(this.getNthWordFromCursor(1));
                  } else {
                     this.moveCursorBy(1);
                  }

                  return true;
               case 207:
                  if (GuiScreen.func_146272_n()) {
                     this.setSelectionPos(this.text.length());
                  } else {
                     this.setCursorPositionEnd();
                  }

                  return true;
               case 211:
                  if (GuiScreen.func_146271_m()) {
                     this.deleteWords(1);
                  } else {
                     this.deleteFromCursor(1);
                  }

                  return true;
               default:
                  if (ChatAllowedCharacters.func_71566_a(cha)) {
                     this.writeText(Character.toString(cha));
                     return true;
                  } else {
                     return false;
                  }
            }
         }
      }
   }

   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      boolean flag = HoveringUtil.isHovering(this.xPosition, this.yPosition, this.width, this.height, mouseX, mouseY);
      if (this.canLoseFocus) {
         this.setFocused(flag);
      }

      if (this.isFocused && flag && mouseButton == 0) {
         float xPos = this.xPosition;
         if (this.backgroundText != null && this.backgroundText.equals("Search")) {
            xPos += 13.0F;
         }

         float i = mouseX - xPos;
         String s = this.font.trimStringToWidth(this.text.substring(this.lineScrollOffset), (int)this.getWidth());
         this.setCursorPosition(this.font.trimStringToWidth(s, (int)i).length() + this.lineScrollOffset);
      }
   }

   public void drawTextBox() {
      if (this.getVisible()) {
         if (this.isFocused()) {
            Keyboard.enableRepeatEvents(true);
         }

         Color textColorWithAlpha = this.focusedTextColor;
         if (this.textAlpha != 1.0F) {
            textColorWithAlpha = ColorUtil.applyOpacity(this.focusedTextColor, this.textAlpha);
         }

         float xPos = this.xPosition + 3.0F;
         float yPos = this.yPosition + this.font.getMiddleOfBox(this.height);
         if (this.isDrawingBackground()) {
            if (this.outline != null) {
               RoundedUtil.drawRound(this.xPosition - 1.0F, this.yPosition - 1.0F, this.width + 2.0F, this.height + 2.0F, this.radius + 1.0F, this.outline);
            }

            RoundedUtil.drawRound(this.xPosition, this.yPosition, this.width, this.height, this.radius, ColorUtil.applyOpacity(this.fill, this.alpha));
         } else {
            float rectHeight = 1.0F;
            RenderUtil.drawRect2(
               this.xPosition,
               this.yPosition + this.height - rectHeight,
               this.width,
               rectHeight,
               ColorUtil.interpolateColor(this.focusedTextColor, this.unfocusedTextColor, this.textColor.getOutput().floatValue())
            );
         }

         this.textColor.setDirection(this.isFocused() ? Direction.BACKWARDS : Direction.FORWARDS);
         if (this.backgroundText != null) {
            Color backgroundTextColor = ColorUtil.applyOpacity(
               ColorUtil.applyOpacity(this.unfocusedTextColor, this.textAlpha), this.textColor.getOutput().floatValue()
            );
            if (this.backgroundText.equals("Search")) {
               Fonts.iconFont16
                  .drawString(
                     "B",
                     xPos + 1.5F,
                     this.yPosition + Fonts.iconFont16.getMiddleOfBox(this.getHeight()),
                     ColorUtil.applyOpacity(this.unfocusedTextColor, this.textAlpha)
                  );
               xPos += 15.0F;
            }

            if (this.text.equals("") && !this.textColor.finished(Direction.BACKWARDS)) {
               this.font.drawString(this.backgroundText, xPos, yPos, backgroundTextColor);
            }
         }

         int cursorPos = this.cursorPosition - this.lineScrollOffset;
         int selEnd = this.selectionEnd - this.lineScrollOffset;
         String text = this.font.trimStringToWidth(this.text.substring(this.lineScrollOffset), (int)this.getWidth());
         boolean cursorInBounds = cursorPos >= 0 && cursorPos <= text.length();
         boolean canShowCursor = this.isFocused && cursorInBounds;
         float j1 = xPos;
         if (selEnd > text.length()) {
            selEnd = text.length();
         }

         if (text.length() > 0) {
            String s1 = cursorInBounds ? text.substring(0, cursorPos) : text;
            j1 = this.font.drawStringWithShadow(s1, xPos, yPos, textColorWithAlpha.getRGB()) + 0.5F;
         }

         boolean cursorEndPos = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
         float k1 = j1;
         if (!cursorInBounds) {
            k1 = cursorPos > 0 ? xPos + this.width : xPos;
         } else if (cursorEndPos) {
            k1 = j1--;
         }

         if (text.length() > 0 && cursorInBounds && cursorPos < text.length()) {
            j1 = this.font.drawStringWithShadow(text.substring(cursorPos), j1 + 2.0F, yPos, textColorWithAlpha.getRGB());
         }

         boolean cursorBlink = this.timerUtil.hasTimeElapsed(2000L) || cursorEndPos;
         if (canShowCursor) {
            if (cursorBlink) {
               if (this.cursorBlinkAnimation.isDone()) {
                  this.cursorBlinkAnimation.changeDirection();
               }
            } else {
               this.cursorBlinkAnimation.setDirection(Direction.FORWARDS);
            }

            RenderUtil.drawRect2(
               k1 + 1.0F,
               yPos - 2.0F,
               0.5,
               this.font.getHeight() + 3,
               ColorUtil.applyOpacity(textColorWithAlpha, this.cursorBlinkAnimation.getOutput().floatValue()).getRGB()
            );
         }

         if (selEnd != cursorPos) {
            int l1 = (int)(xPos + this.font.getStringWidth(text.substring(0, selEnd)));
            int offset = selEnd > cursorPos ? 2 : 0;
            float widthOffset = selEnd > cursorPos ? 0.5F : 0.0F;
            this.drawSelectionBox(k1 + offset, yPos - 1.0F, l1 + widthOffset, yPos + 1.0F + this.font.getHeight());
         }
      }
   }

   private void drawSelectionBox(float x, float y, float width, float height) {
      RenderUtil.drawRect2(Math.min(x,width),Math.min(y,height),Math.abs(width-x),Math.abs(height-y),0x805577FF);
   }

   public void setMaxStringLength(int len) {
      this.maxStringLength = len;
      if (this.text.length() > len) {
         this.text = this.text.substring(0, len);
      }
   }

   public int getMaxStringLength() {
      return this.maxStringLength;
   }

   public int getCursorPosition() {
      return this.cursorPosition;
   }

   public void setTextColor(Color color) {
      this.focusedTextColor = color;
   }

   public void setDisabledTextColour(Color color) {
      this.unfocusedTextColor = color;
   }

   public int getSelectionEnd() {
      return this.selectionEnd;
   }

   public float getWidth() {
      boolean flag = this.backgroundText != null && this.backgroundText.equals("Search");
      return this.isDrawingBackground() ? this.width - (flag ? 17 : 4) : this.width;
   }

   public float getRealWidth() {
      return this.isDrawingBackground() ? this.width - 4.0F : this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public void setSelectionPos(int selectionPos) {
      int i = this.text.length();
      if (selectionPos > i) {
         selectionPos = i;
      }

      if (selectionPos < 0) {
         selectionPos = 0;
      }

      this.selectionEnd = selectionPos;
      if (this.font != null) {
         if (this.lineScrollOffset > i) {
            this.lineScrollOffset = i;
         }

         float j = this.getWidth();
         String s = this.font.trimStringToWidth(this.text.substring(this.lineScrollOffset), (int)j);
         int k = s.length() + this.lineScrollOffset;
         if (selectionPos == this.lineScrollOffset) {
            this.lineScrollOffset = this.lineScrollOffset - this.font.trimStringToWidth(this.text, (int)j, true).length();
         }

         if (selectionPos > k) {
            this.lineScrollOffset += selectionPos - k;
         } else if (selectionPos <= this.lineScrollOffset) {
            this.lineScrollOffset = this.lineScrollOffset - (this.lineScrollOffset - selectionPos);
         }

         this.lineScrollOffset = MathHelper.func_76125_a(this.lineScrollOffset, 0, i);
      }
   }

   public void setCanLoseFocus(boolean canLoseFocus) {
      this.canLoseFocus = canLoseFocus;
   }

   public boolean getVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void setFont(CustomFont font) {
      this.font = font;
   }

   public float getXPosition() {
      return this.xPosition;
   }

   public void setXPosition(float xPosition) {
      this.xPosition = xPosition;
   }

   public float getYPosition() {
      return this.yPosition;
   }

   public void setYPosition(float yPosition) {
      this.yPosition = yPosition;
   }

   public float getRadius() {
      return this.radius;
   }

   public void setRadius(float radius) {
      this.radius = radius;
   }

   public float getAlpha() {
      return this.alpha;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public void setTextAlpha(float textAlpha) {
      this.textAlpha = textAlpha;
   }

   public Color getOutline() {
      return this.outline;
   }

   public void setOutline(Color outline) {
      this.outline = outline;
   }

   public Color getFill() {
      return this.fill;
   }

   public void setFill(Color fill) {
      this.fill = fill;
   }

   public void setBackgroundText(String backgroundText) {
      this.backgroundText = backgroundText;
   }

   public boolean isDrawingBackground() {
      return this.drawingBackground;
   }

   public void setDrawingBackground(boolean drawingBackground) {
      this.drawingBackground = drawingBackground;
   }

   public boolean isFocused() {
      return this.isFocused;
   }

   public void setFocused(boolean isFocused) {
      this.isFocused = isFocused;
   }
}
