package wtf.tatp.meowtils.extension;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Async JSON helper for extensions such as Stats; never blocks the client thread. */
public final class HttpJson {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private HttpJson() {}

    public static CompletableFuture<JsonObject> get(URI uri, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12)).GET();
        headers.forEach(request::header);
        return CLIENT.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() / 100 != 2) throw new IllegalStateException("HTTP " + response.statusCode() + " from " + uri);
                    return JsonParser.parseString(response.body()).getAsJsonObject();
                });
    }

    public static CompletableFuture<Path> download(URI uri, Path dest, Map<String, String> headers) {
        try {
            if (dest.getParent() != null) Files.createDirectories(dest.getParent());
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60)).GET();
        headers.forEach(request::header);
        return CLIENT.sendAsync(request.build(), HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    try (var body = response.body()) {
                        if (response.statusCode() / 100 != 2) {
                            throw new IllegalStateException("HTTP " + response.statusCode() + " from " + uri);
                        }
                        Files.copy(body, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (RuntimeException exception) {
                        throw exception;
                    } catch (Exception exception) {
                        throw new IllegalStateException("Failed to write " + dest, exception);
                    }
                    return dest;
                });
    }
}
