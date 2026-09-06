package meowtils.tenacitygui.tenacity.search;

import meowtils.tenacitygui.tenacity.Screen;
import meowtils.tenacitygui.tenacity.TenacityClickGUI;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.TextField;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.render.GuiScreen;
import wtf.tatp.meowtils.extension.render.ScaledResolution;

public class SearchBar implements Screen {
   private static final Minecraft mc = Minecraft.getInstance();
   private boolean focused;
   private boolean typing;
   private boolean hoveringBottomOfScreen;
   private final Animation focusAnimation = new DecelerateAnimation(175, 1.0).setDirection(Direction.BACKWARDS);
   private final Animation hoverAnimation = new DecelerateAnimation(175, 1.0).setDirection(Direction.BACKWARDS);
   private final Animation openAnimation = new DecelerateAnimation(250, 1.0).setDirection(Direction.BACKWARDS);
   private TextField searchField;
   private float alpha;

   public TextField getSearchField() {
      if (this.searchField == null) {
         this.searchField = new TextField(Fonts.tenacityFont18);
      }

      return this.searchField;
   }

   public boolean isFocused() {
      return this.focused;
   }

   public boolean isTyping() {
      return this.typing;
   }

   public boolean isFiltering() {
      return this.focused && !this.getSearchField().getText().isEmpty();
   }

   public Animation getFocusAnimation() {
      return this.focusAnimation;
   }

   public Animation getOpenAnimation() {
      return this.openAnimation;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   @Override
   public void initGui() {
      this.openAnimation.setDirection(Direction.FORWARDS);
      this.getSearchField().setText("");
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      TextField searchField = this.getSearchField();
      if (keyCode == 1) {
         searchField.setFocused(false);
      } else if (GuiScreen.func_146271_m() && keyCode == 33) {
         searchField.setFocused(true);
         TenacityClickGUI.collapseAll();
      } else {
         searchField.keyTyped(typedChar, keyCode);
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY) {
      TextField searchField = this.getSearchField();
      this.focused = searchField.isFocused() || !searchField.getText().isEmpty();
      this.typing = searchField.isFocused();
      ScaledResolution sr = new ScaledResolution(mc);
      float width = sr.func_78326_a();
      this.hoveringBottomOfScreen = HoveringUtil.isHovering(width / 2.0F - 120.0F, sr.func_78328_b() - 100, 240.0F, 100.0F, mouseX, mouseY);
      this.hoverAnimation.setDirection(this.hoveringBottomOfScreen && !this.focused ? Direction.FORWARDS : Direction.BACKWARDS);
      this.focusAnimation.setDirection(this.focused ? Direction.FORWARDS : Direction.BACKWARDS);
      float focusAnim = this.focusAnimation.getOutput().floatValue();
      float hover = this.hoverAnimation.getOutput().floatValue();
      float openAnim = Math.min(1.0F, this.alpha);
      float searchAlpha = Math.min(1.0F, hover + focusAnim);
      Fonts.tenacityFont26
         .drawCenteredString(
            "Do §lCTRL§r+§lF§r to open the search bar",
            sr.func_78326_a() / 2.0F,
            sr.func_78328_b() - 75,
            ColorUtil.applyOpacity(-1, 0.3F * (1.0F - searchAlpha) * openAnim)
         );
      searchField.setWidth(200.0F);
      searchField.setHeight(25.0F);
      searchField.setFont(Fonts.tenacityFont24);
      searchField.setXPosition(sr.func_78326_a() / 2.0F - 100.0F);
      searchField.setYPosition(sr.func_78328_b() - (70.0F + 25.0F * hover + 60.0F * focusAnim));
      searchField.setRadius(5.0F);
      searchField.setAlpha(Math.max(hover * 0.85F, focusAnim));
      searchField.setTextAlpha(searchField.getAlpha());
      searchField.setFill(ColorUtil.tripleColor(17));
      searchField.setOutline(null);
      searchField.setBackgroundText("Search");
      searchField.drawTextBox();
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      TextField searchField = this.getSearchField();
      boolean wasFocused = searchField.isFocused();
      searchField.mouseClicked(mouseX, mouseY, button);
      if (!wasFocused && searchField.isFocused()) {
         TenacityClickGUI.collapseAll();
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int state) {
   }
}
