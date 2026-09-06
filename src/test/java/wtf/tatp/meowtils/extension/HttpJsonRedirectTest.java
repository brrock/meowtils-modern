package wtf.tatp.meowtils.extension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HttpJsonRedirectTest {
    @Test
    void downloadFollowsGithubStyleRedirect(@TempDir java.nio.file.Path temp) throws Exception {
        byte[] jar = "meowtils-update".getBytes(StandardCharsets.UTF_8);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/releases/download/v1.0.2/meowtils-1.0.2.jar", exchange -> {
            exchange.getResponseHeaders().add("Location", "/cdn/meowtils-1.0.2.jar");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/cdn/meowtils-1.0.2.jar", exchange -> {
            exchange.sendResponseHeaders(200, jar.length);
            exchange.getResponseBody().write(jar);
            exchange.close();
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                    + "/releases/download/v1.0.2/meowtils-1.0.2.jar");
            var dest = temp.resolve("meowtils-1.0.2.jar");
            assertEquals(dest, HttpJson.download(uri, dest, Map.of("User-Agent", "Meowtils-Updater")).join());
            assertArrayEquals(jar, Files.readAllBytes(dest));
        } finally {
            server.stop(0);
        }
    }
}
