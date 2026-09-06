package meowtils.tenacitygui.tenacity.panel;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.Screen;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.Pair;

public abstract class SettingComponent implements Screen {
   public static float allowedClickGuiHeight = 200.0F;
   public float x;
   public float y;
   public float width;
   public float height;
   public float alpha;
   public boolean typing;
   public float panelLimitY;
   public Pair<Color, Color> clientColors;
   public Color settingRectColor;
   public Color textColor;
   public float countSize = 1.0F;

   public abstract String getName();

   public boolean isHoveringBox(int mouseX, int mouseY) {
      return HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY);
   }

   public boolean isClickable(float bottomY) {
      return bottomY > this.panelLimitY && bottomY < this.panelLimitY + 17.0F + allowedClickGuiHeight;
   }
}
