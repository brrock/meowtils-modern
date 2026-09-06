package meowtils.notifications.tenacity.util;
import java.io.InputStream;
public final class ExtensionResources {
 private ExtensionResources() {}
 public static InputStream open(String path){
  try{return wtf.tatp.meowtils.extension.ExtensionResources.open(ExtensionResources.class,path);}
  catch(java.io.IOException error){throw new IllegalStateException("Missing extension resource: "+path,error);}
 }
 public static byte[] read(String path){try(var stream=open(path)){return stream.readAllBytes();}catch(java.io.IOException error){throw new IllegalStateException(error);}}
}
