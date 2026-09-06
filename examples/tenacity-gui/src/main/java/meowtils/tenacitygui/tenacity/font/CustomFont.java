package meowtils.tenacitygui.tenacity.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import meowtils.tenacitygui.tenacity.render.GLUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.util.MathUtils;
import meowtils.tenacitygui.tenacity.util.MutablePair;
import wtf.tatp.meowtils.extension.render.GlStateManager;
import wtf.tatp.meowtils.extension.render.DynamicTexture;
import wtf.tatp.meowtils.extension.render.GL11;

public class CustomFont implements AbstractFontRenderer {
   private static int[] colorCode;
   private static final String colorcodeIdentifiers = "0123456789abcdefklmnor";
   private final Font font;
   private CustomFont boldFont;
   private final CustomFont.FontData regular = new CustomFont.FontData(0);
   private final CustomFont.FontData italic = new CustomFont.FontData(2);
   private int fontHeight;
   private static final float KERNING = 8.2F;
   private boolean smooth;
   private final List<String> lines = new ArrayList<>();

   public void setSmooth(boolean smooth) {
      this.smooth = smooth;
   }

   public CustomFont(Font font) {
      this.generateColorCodes();
      this.font = font;
      this.setupTexture(this.regular);
      this.setupTexture(this.italic);
   }

   public CustomFont getBoldFont() {
      return this.boldFont;
   }

   public void setBoldFont(CustomFont boldFont) {
      this.boldFont = boldFont;
   }

   private void setupTexture(CustomFont.FontData fontData) {
      BufferedImage fakeImage = new BufferedImage(1, 1, 2);
      Graphics2D graphics = (Graphics2D)fakeImage.getGraphics();
      Font currentFont = fontData.textType == 0 ? this.font : this.font.deriveFont(fontData.textType);
      graphics.setFont(currentFont);
      this.handleSprites(fontData, currentFont, graphics);
   }

   public void drawSmoothString(String text, double x2, float y2, int color) {
      this.drawString(text, x2, y2, color, false, 8.2F, true);
   }

   public void drawSmoothStringWithShadow(String text, double x2, float y2, int color) {
      this.drawString(text, x2 + 0.5, y2 + 0.5F, color, true, 8.2F, true);
      this.drawString(text, x2, y2, color, false, 8.2F, true);
   }

   @Override
   public int drawCenteredString(String name, float x, float y, int color) {
      return this.drawString(name, x - this.getStringWidth(name) / 2.0F, y, color);
   }

   @Override
   public void drawCenteredString(String name, float x, float y, Color color) {
      this.drawCenteredString(name, x, y, color.getRGB());
   }

   public void drawCenteredStringWithShadow(String text, float x, float y, int color) {
      this.drawStringWithShadow(text, x - this.getStringWidth(text) / 2.0F, y, color);
   }

   @Override
   public int drawStringWithShadow(String name, float x, float y, int color) {
      this.drawString(name, x + 0.5F, y + 0.5F, color, true, 8.2F, this.smooth);
      return (int)this.drawString(name, x, y, color, false, 8.2F, this.smooth);
   }

   @Override
   public void drawStringWithShadow(String name, float x, float y, Color color) {
      this.drawStringWithShadow(name, x, y, color.getRGB());
   }

   @Override
   public int drawString(String text, float x, float y, int color, boolean shadow) {
      return shadow ? this.drawStringWithShadow(text, x, y, color) : (int)this.drawString(text, x, y, color, false, 8.2F, this.smooth);
   }

   @Override
   public int drawString(String name, float x, float y, int color) {
      return this.drawString(name, x, y, color, false);
   }

   @Override
   public void drawString(String name, float x, float y, Color color) {
      this.drawString(name, x, y, color.getRGB(), false);
   }

   public float drawString(String text, double x, double y, int color, boolean shadow, float kerning, boolean smooth) {
      if (text == null) {
         return 0.0F;
      } else {
         if (shadow) {
            color = (color & 16579836) >> 2 | color & 0xFF000000;
         }

         GlStateManager.func_179094_E();
         GlStateManager.func_179139_a(0.5, 0.5, 0.5);
         GLUtil.startBlend();
         RenderUtil.resetColor();
         RenderUtil.color(color);
         GlStateManager.func_179098_w();
         GlStateManager.func_179144_i(this.regular.texture.func_110552_b());
         if (smooth) {
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
         } else {
            GL11.glTexParameteri(3553, 10241, 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
         }

         float returnVal = this.drawCustomChars(text, x, y, kerning, color, shadow);
         GL11.glHint(3155, 4352);
         GlStateManager.func_179121_F();
         RenderUtil.resetColor();
         GlStateManager.func_179144_i(0);
         return returnVal;
      }
   }

   private float drawCustomChars(String text, double x, double y, float kerning, int color, boolean shadow) {
      x = (x - 1.0) * 2.0;
      y = (y - 3.0) * 2.0;
      CustomFont.FontData currentData = this.regular;
      float alpha = (color >> 24 & 0xFF) / 255.0F;
      boolean bold = false;
      boolean italic = false;
      boolean strikethrough = false;
      boolean underline = false;

      for (int index = 0; index < text.length(); index++) {
         char character = text.charAt(index);
         if (character == 167) {
            int colorIndex = 21;

            try {
               colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(index + 1));
            } catch (Exception var19) {
               var19.printStackTrace();
            }

            if (colorIndex < 16) {
               bold = false;
               italic = false;
               underline = false;
               strikethrough = false;
               GlStateManager.func_179144_i(this.regular.texture.func_110552_b());
               currentData = this.regular;
               if (colorIndex < 0) {
                  colorIndex = 15;
               }

               if (shadow) {
                  colorIndex += 16;
               }

               RenderUtil.color(colorCode[colorIndex], alpha);
            } else {
               switch (colorIndex) {
                  case 17:
                     if (this.hasBoldFont()) {
                        bold = true;
                        if (italic) {
                           GlStateManager.func_179144_i(this.boldFont.italic.texture.func_110552_b());
                           currentData = this.boldFont.italic;
                        } else {
                           GlStateManager.func_179144_i(this.boldFont.regular.texture.func_110552_b());
                           currentData = this.boldFont.regular;
                        }
                     }
                     break;
                  case 18:
                     strikethrough = true;
                     break;
                  case 19:
                     underline = true;
                     break;
                  case 20:
                     italic = true;
                     if (bold && this.hasBoldFont()) {
                        GlStateManager.func_179144_i(this.boldFont.italic.texture.func_110552_b());
                        currentData = this.boldFont.italic;
                        break;
                     }

                     GlStateManager.func_179144_i(this.italic.texture.func_110552_b());
                     currentData = this.italic;
                     break;
                  default:
                     bold = false;
                     italic = false;
                     underline = false;
                     strikethrough = false;
                     RenderUtil.color(color);
                     GlStateManager.func_179144_i(this.regular.texture.func_110552_b());
                     currentData = this.regular;
               }
            }

            index++;
         } else if (character < currentData.chars.length) {
            this.drawLetter(x, y, currentData, strikethrough, underline, character);
            x += MathUtils.roundToHalf(currentData.chars[character].width - 8.2F);
         }
      }

      return (float)(x / 2.0);
   }

   public void drawLetter(double x, double y, CustomFont.FontData currentData, boolean strikethrough, boolean underline, char character) {
      GL11.glBegin(4);
      CustomFont.CharData charData = currentData.chars[character];
      this.drawQuad(
         (float)x,
         (float)y,
         charData.width,
         charData.height,
         charData.storedX,
         charData.storedY,
         currentData.imageSize.getFirst().intValue(),
         currentData.imageSize.getSecond().intValue()
      );
      GL11.glEnd();
      if (strikethrough) {
         this.drawLine(x, y + charData.height / 2, x + charData.width - 8.0, y + charData.height / 2);
      }

      if (underline) {
         this.drawLine(x + 2.5, y + charData.height - 1.0, x + charData.width - 6.0, y + charData.height - 1.0);
      }
   }

   protected void drawQuad(float x2, float y2, float width, float height, float srcX, float srcY, float imgWidth, float imgHeight) {
      float renderSRCX = srcX / imgWidth;
      float renderSRCY = srcY / imgHeight;
      float renderSRCWidth = width / imgWidth;
      float renderSRCHeight = height / imgHeight;
      GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
      GL11.glVertex2d(x2 + width, y2);
      GL11.glTexCoord2f(renderSRCX, renderSRCY);
      GL11.glVertex2d(x2, y2);
      GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
      GL11.glVertex2d(x2, y2 + height);
      GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
      GL11.glVertex2d(x2, y2 + height);
      GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
      GL11.glVertex2d(x2 + width, y2 + height);
      GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
      GL11.glVertex2d(x2 + width, y2);
   }

   @Override
   public float getMiddleOfBox(float height) {
      return height / 2.0F - this.getHeight() / 2.0F;
   }

   @Override
   public String trimStringToWidth(String text, int width) {
      return this.trimStringToWidth(text, width, false);
   }

   @Override
   public String trimStringToWidth(String text, int width, boolean reverse) {
      if (text == null) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder();
         float lineWidth = 0.0F;
         int offset = reverse ? text.length() - 1 : 0;
         int increment = reverse ? -1 : 1;
         boolean var8 = false;
         boolean var9 = false;

         for (int index = offset; index >= 0 && index < text.length() && lineWidth < width; index += increment) {
            char character = text.charAt(index);
            float charWidth = this.getCharWidthFloat(character);
            if (var8) {
               var8 = false;
               if (character == 'l' || character == 'L') {
                  var9 = true;
               } else if (character == 'r' || character == 'R') {
                  var9 = false;
               }
            } else if (charWidth < 0.0F) {
               var8 = true;
            } else {
               lineWidth += charWidth;
               if (var9) {
                  lineWidth++;
               }
            }

            if (lineWidth > width) {
               break;
            }

            if (reverse) {
               buffer.insert(0, character);
            } else {
               buffer.append(character);
            }
         }

         return buffer.toString();
      }
   }

   private float getCharWidthFloat(char c) {
      if (c == 167) {
         return -1.0F;
      } else if (c == ' ') {
         return 2.0F;
      } else {
         int var2 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000"
            .indexOf(c);
         if (c > 0 && var2 != -1) {
            return this.regular.chars[var2].width / 2.0F - 4.0F;
         } else if (c < this.regular.chars.length && this.regular.chars[c].width / 2.0F - 4.0F != 0.0F) {
            int var3 = (int)(this.regular.chars[c].width / 2.0F - 4.0F) >>> 4;
            int var4 = (int)(this.regular.chars[c].width / 2.0F - 4.0F) & 15;
            var3 &= 15;
            var4++;
            return (var4 - var3) / 2 + 1;
         } else {
            return 0.0F;
         }
      }
   }

   @Override
   public int getHeight() {
      return (this.fontHeight - 8) / 2;
   }

   @Override
   public float getStringWidth(String text) {
      return (float)this.getStringWidth(text, 8.2F);
   }

   public double getStringWidth(String text, float kerning) {
      if (text == null) {
         return 0.0;
      } else {
         float width = 0.0F;
         CustomFont.CharData[] currentData = this.regular.chars;

         for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == 167) {
               int colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(index + 1));
               switch (colorIndex) {
                  case 17:
                     if (this.hasBoldFont()) {
                        currentData = this.boldFont.regular.chars;
                     }
                     break;
                  case 20:
                     currentData = this.regular.chars;
                     break;
                  default:
                     currentData = this.regular.chars;
               }

               index++;
            } else if (character < currentData.length) {
               width += currentData[character].width - kerning;
            }
         }

         return width / 2.0F;
      }
   }

   public boolean hasBoldFont() {
      return this.boldFont != null;
   }

   public List<String> getWrappedLines(String text, float x, float width, float heightIncrement) {
      this.wrapTextToLines(text, x, width);
      return this.lines;
   }

   public float drawWrappedText(String text, float x, float y, int color, float width, float heightIncrement) {
      this.wrapTextToLines(text, x, width);
      float newY = y;

      for (String s : this.lines) {
         RenderUtil.resetColor();
         this.drawString(s, x, newY, color);
         newY += this.getHeight() + heightIncrement;
      }

      return newY - y;
   }

   public MutablePair<Float, Float> drawNewLineText(String text, float x, float y, int color, float heightIncrement) {
      this.wrapTextToNewLine(text, x);
      String longest = "";
      float newY = y;

      for (String s : this.lines) {
         if (this.getStringWidth(s) > this.getStringWidth(longest)) {
            longest = s;
         }

         RenderUtil.resetColor();
         this.drawString(s, x, newY, color);
         newY += this.getHeight() + heightIncrement;
      }

      return MutablePair.of(this.getStringWidth(longest), newY - y);
   }

   private void wrapTextToNewLine(String text, float x) {
      this.lines.clear();
      this.lines.addAll(Arrays.asList(text.trim().split("\n")));
   }

   private void wrapTextToLines(String text, float x, float width) {
      this.lines.clear();
      String[] words = text.trim().split(" ");
      StringBuilder line = new StringBuilder();

      for (String word : words) {
         float totalWidth = this.getStringWidth(line + " " + word);
         if (x + totalWidth >= x + width) {
            this.lines.add(line.toString());
            line = new StringBuilder(word).append(" ");
         } else {
            line.append(word).append(" ");
         }
      }

      this.lines.add(line.toString());
   }

   private void drawLine(double x2, double y2, double x1, double y1) {
      GL11.glDisable(3553);
      GL11.glLineWidth(1.0F);
      GL11.glBegin(1);
      GL11.glVertex2d(x2, y2);
      GL11.glVertex2d(x1, y1);
      GL11.glEnd();
      GL11.glEnable(3553);
   }

   private void generateColorCodes() {
      if (colorCode == null) {
         colorCode = new int[32];

         for (int i = 0; i < 32; i++) {
            int noClue = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + noClue;
            int green = (i >> 1 & 1) * 170 + noClue;
            int blue = (i & 1) * 170 + noClue;
            if (i == 6) {
               red += 85;
            }

            if (i >= 16) {
               red /= 4;
               green /= 4;
               blue /= 4;
            }

            colorCode[i] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
         }
      }
   }

   private void handleSprites(CustomFont.FontData fontData, Font currentFont, Graphics2D graphics2D) {
      this.handleSprites(fontData, currentFont, graphics2D, false);
   }

   private void handleSprites(CustomFont.FontData fontData, Font currentFont, Graphics2D graphics2D, boolean drawString) {
      int charHeight = 0;
      int positionX = 0;
      int positionY = 1;
      int index = 0;
      FontMetrics fontMetrics = graphics2D.getFontMetrics();
      if (drawString) {
         BufferedImage image = new BufferedImage(fontData.imageSize.getFirst(), fontData.imageSize.getSecond(), 2);
         Graphics2D graphics = (Graphics2D)image.getGraphics();
         graphics.setFont(currentFont);
         graphics.setColor(new Color(255, 255, 255, 0));
         graphics.fillRect(0, 0, fontData.imageSize.getFirst(), fontData.imageSize.getSecond());
         graphics.setColor(Color.WHITE);
         graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

         for (CustomFont.CharData data : fontData.chars) {
            char c = (char)index;
            graphics.drawString(String.valueOf(c), data.storedX + 2, data.storedY + fontMetrics.getAscent());
            index++;
         }

         fontData.texture = new DynamicTexture(image);
      } else {
         while (index < fontData.chars.length) {
            char c = (char)index;
            CustomFont.CharData charData = new CustomFont.CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(c), graphics2D);
            charData.width = dimensions.getBounds().width + 8.2F;
            charData.height = dimensions.getBounds().height;
            if (positionX + charData.width >= fontData.imageSize.getFirst().intValue()) {
               positionX = 0;
               positionY += charHeight;
               charHeight = 0;
            }

            if (charData.height > charHeight) {
               charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;
            if (charData.height > this.fontHeight) {
               this.fontHeight = charData.height;
            }

            fontData.chars[index] = charData;
            positionX = (int)(positionX + charData.width);
            fontData.imageSize.setSecond(positionY + fontMetrics.getAscent());
            index++;
         }

         this.handleSprites(fontData, currentFont, graphics2D, true);
      }
   }

   private static class CharData {
      private float width;
      private int height;
      private int storedX;
      private int storedY;

      private CharData() {
      }
   }

   private static class FontData {
      private final CustomFont.CharData[] chars = new CustomFont.CharData[256];
      private final int textType;
      private DynamicTexture texture;
      private final MutablePair<Integer, Integer> imageSize = MutablePair.of(512, 0);

      public FontData(int textType) {
         this.textType = textType;
      }
   }
}
