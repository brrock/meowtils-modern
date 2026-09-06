package wtf.tatp.meowtils.extension.render;
public final class ChatAllowedCharacters {
 public static boolean func_71566_a(char c){return c>=32&&c!=127&&c!=167;}
 public static String func_71565_a(String text){StringBuilder out=new StringBuilder();for(char c:text.toCharArray())if(func_71566_a(c))out.append(c);return out.toString();}
}
