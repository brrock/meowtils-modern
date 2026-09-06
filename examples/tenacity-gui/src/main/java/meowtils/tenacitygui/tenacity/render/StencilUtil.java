package meowtils.tenacitygui.tenacity.render;
public final class StencilUtil {
 public static void initStencilToWrite(){wtf.tatp.meowtils.extension.render.Shapes.beginMask();}
 public static void readStencilBuffer(int ref){wtf.tatp.meowtils.extension.render.Shapes.useMask();}
 public static void uninitStencilBuffer(){wtf.tatp.meowtils.extension.render.Shapes.endMask();}
}
