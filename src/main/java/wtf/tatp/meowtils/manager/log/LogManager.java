package wtf.tatp.meowtils.manager.log;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsData;

/** Appends timestamped lines to {@code meowtils/meowtils.log}. */
public final class LogManager {
    private static boolean initialized;

    private LogManager() {}

    public static void init() {
        MeowtilsData.ensure();
        write("INFO", "Meowtils log started");
        initialized = true;
    }

    public static void write(String level, String message) {
        if (level == null || message == null) return;
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String line = "[" + timestamp + "] [" + level + "]: " + message + System.lineSeparator();
        Path file = MeowtilsData.logFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            initialized = true;
        } catch (Exception exception) {
            Meowtils.error("Unable to write to meowtils.log: " + exception);
        }
    }

    public static Path findLog() {
        Path file = MeowtilsData.logFile();
        return Files.isRegularFile(file) ? file : null;
    }

    public static void open() {
        Path log = findLog();
        Path folder = log != null ? log.getParent() : MeowtilsData.root();
        if (folder == null) folder = MeowtilsData.root();
        Meowtils.openFolder(folder, "log");
        Meowtils.info("Opened log.");
        Meowtils.addMessage("Opened log.");
    }
}
