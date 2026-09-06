package meowtils.notifications.tenacity.font;

import java.awt.Color;

public interface AbstractFontRenderer {
   float getStringWidth(String var1);

   int drawStringWithShadow(String var1, float var2, float var3, int var4);

   void drawStringWithShadow(String var1, float var2, float var3, Color var4);

   int drawCenteredString(String var1, float var2, float var3, int var4);

   void drawCenteredString(String var1, float var2, float var3, Color var4);

   String trimStringToWidth(String var1, int var2);

   String trimStringToWidth(String var1, int var2, boolean var3);

   int drawString(String var1, float var2, float var3, int var4, boolean var5);

   void drawString(String var1, float var2, float var3, Color var4);

   int drawString(String var1, float var2, float var3, int var4);

   float getMiddleOfBox(float var1);

   int getHeight();
}
