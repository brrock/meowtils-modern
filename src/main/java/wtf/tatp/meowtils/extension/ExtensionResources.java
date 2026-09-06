package wtf.tatp.meowtils.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Small classloader-safe resource helper for extension assets. */
public final class ExtensionResources {
    private ExtensionResources() {}

    public static URL url(Class<?> owner, String path) {
        URL resource = owner.getResource(normalize(path));
        if (resource == null) throw new IllegalArgumentException("Missing extension resource: " + path);
        return resource;
    }

    public static InputStream open(Class<?> owner, String path) throws IOException {
        InputStream stream = owner.getResourceAsStream(normalize(path));
        if (stream == null) throw new IOException("Missing extension resource: " + path);
        return stream;
    }

    private static String normalize(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}
