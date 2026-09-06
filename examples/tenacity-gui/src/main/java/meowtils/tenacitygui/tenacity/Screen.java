package meowtils.tenacitygui.tenacity;

public interface Screen {
   default void onDrag(int mouseX, int mouseY) {
   }

   void initGui();

   void keyTyped(char var1, int var2);

   void drawScreen(int var1, int var2);

   void mouseClicked(int var1, int var2, int var3);

   void mouseReleased(int var1, int var2, int var3);
}
