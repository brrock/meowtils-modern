package wtf.tatp.meowtils.update;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Replaces the installed mod jar after Minecraft has exited. */
public final class MeowtilsAutoUpdate {
    private MeowtilsAutoUpdate() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: MeowtilsAutoUpdate <old-jar> <new-jar>");
            System.exit(1);
        }
        Path oldJar = Path.of(args[0]);
        Path newJar = Path.of(args[1]);
        if (!Files.isRegularFile(newJar)) {
            System.err.println("New jar missing: " + newJar);
            System.exit(2);
        }
        Thread.sleep(1500L);
        Exception last = null;
        for (int attempt = 0; attempt < 40; attempt++) {
            try {
                Files.copy(newJar, oldJar, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(newJar);
                return;
            } catch (Exception exception) {
                last = exception;
                Thread.sleep(500L);
            }
        }
        if (last != null) last.printStackTrace();
        System.exit(3);
    }
}
