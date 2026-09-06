package wtf.tatp.meowtils.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import wtf.tatp.meowtils.MeowtilsClient;
import wtf.tatp.meowtils.MeowtilsData;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;

/** Loads isolated `.meowtils` archives from the game `meowtils/extensions` folder. */
public final class ExtensionManager {
    private static final Pattern MAIN_CLASS = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
    private static final List<URLClassLoader> openLoaders = new ArrayList<>();
    private static final List<Module> extensionModules = new ArrayList<>();
    private static final List<Object> extensionListeners = new ArrayList<>();
    private static final ThreadLocal<LoadTransaction> transaction = new ThreadLocal<>();
    private static Path directory;
    private ExtensionManager() {}

    public static void initialize() {
        MeowtilsData.ensure();
        directory = MeowtilsData.extensions();
        try { loadAll(); }
        catch (IOException exception) { System.err.println("Meowtils: unable to list extensions: " + exception); }
    }
    public static Path directory() { return directory == null ? MeowtilsData.extensions() : directory; }
    public static List<Module> modules() { return List.copyOf(extensionModules); }
    public static void tick() {}

    public static synchronized void shutdown() {
        for (Object listener : extensionListeners) {
            EventManager.unregister(listener);
            if (listener instanceof AutoCloseable closeable) try { closeable.close(); }
            catch (Exception error) { System.err.println("Meowtils extension cleanup failed: " + error); }
        }
        for (Module module : extensionModules) {
            try { module.setState(false); } catch (Throwable ignored) {}
            ModuleManager.unregister(module);
        }
        extensionListeners.clear();
        extensionModules.clear();
        for (URLClassLoader loader : openLoaders) {
            wtf.tatp.meowtils.extension.render.DynamicTexture.releaseLoader(loader);
            try { loader.close(); } catch (IOException ignored) {}
        }
        openLoaders.clear();
    }

    public static synchronized void loadAll() throws IOException {
        MeowtilsData.ensure();
        directory = MeowtilsData.extensions();
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            for (Path file : files.sorted().toList()) {
                if (!isCandidate(file)) continue;
                try { load(file); }
                catch (Throwable error) { System.err.println("Meowtils: failed to load " + file.getFileName() + ": " + error.getMessage()); }
            }
        }
    }

    public static synchronized void reload() {
        ConfigManager.save();
        shutdown();
        try {
            loadAll();
            ConfigManager.load();
            System.out.println("Meowtils extensions reloaded.");
        } catch (IOException error) {
            System.err.println("Meowtils: extension reload failed: " + error);
        }
    }

    static void trackModule(Module module) { if (transaction.get() != null) transaction.get().modules.add(module); else extensionModules.add(module); }
    static void trackListener(Object listener) { EventManager.register(listener); if (transaction.get() != null) transaction.get().listeners.add(listener); else extensionListeners.add(listener); }

    private static void load(Path file) throws Exception {
        Path archive = resolveInside(file);
        Properties metadata = readMetadata(archive);
        String main = metadata.getProperty("main", "").trim();
        if (!MAIN_CLASS.matcher(main).matches()) throw new IllegalStateException(archive.getFileName() + " has an invalid main class");
        if (looksLikeLegacy189(archive, metadata)) {
            throw new IllegalStateException(archive.getFileName() + " looks like a 1.8.9/Forge Meowtils extension and will not be loaded. Rebuild it against the 26.2 Extension API.");
        }
        URLClassLoader loader = new URLClassLoader(new URL[]{archive.toUri().toURL()}, MeowtilsClient.class.getClassLoader());
        LoadTransaction current = new LoadTransaction();
        transaction.set(current);
        try {
            Class<?> entrypoint = Class.forName(main, true, loader);
            var init = entrypoint.getMethod("init");
            if (init.getReturnType() != void.class || init.getParameterCount() != 0
                    || !java.lang.reflect.Modifier.isStatic(init.getModifiers())
                    || !java.lang.reflect.Modifier.isPublic(init.getModifiers())) {
                throw new IllegalStateException(main + " must expose public static void init()");
            }
            init.invoke(null);
            extensionModules.addAll(current.modules);
            extensionListeners.addAll(current.listeners);
            openLoaders.add(loader);
        } catch (Throwable error) {
            for (Object listener : current.listeners) EventManager.unregister(listener);
            for (Module module : current.modules) { module.setState(false); ModuleManager.unregister(module); }
            loader.close();
            throw new IllegalStateException("Failed to load " + archive.getFileName() + " (" + error.getClass().getSimpleName() + "). Only 26.2-built .meowtils archives with Extension.init() are supported.", error);
        } finally {
            transaction.remove();
        }
    }

    private static Properties readMetadata(Path file) throws IOException {
        try (JarFile jar = new JarFile(file.toFile(), true)) {
            rejectZipSlip(jar);
            var entry = jar.getJarEntry("META-INF/meowtils.extension");
            if (entry == null || entry.isDirectory()) throw new IllegalStateException("Missing META-INF/meowtils.extension");
            Properties metadata = new Properties();
            try (InputStream stream = jar.getInputStream(entry)) { metadata.load(stream); }
            return metadata;
        }
    }

    private static void rejectZipSlip(JarFile jar) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName().replace('\\', '/');
            if (name.startsWith("/") || name.contains("..")) {
                throw new IllegalStateException("Unsafe zip entry: " + name);
            }
        }
    }

    private static Path resolveInside(Path file) throws IOException {
        Path root = directory.toRealPath();
        Path real = file.toRealPath();
        if (!real.startsWith(root) || !Files.isRegularFile(real)) {
            throw new IllegalStateException("Extension is not a regular file inside " + root);
        }
        return real;
    }

    private static boolean looksLikeLegacy189(Path file, Properties metadata) {
        String target = metadata.getProperty("minecraft", metadata.getProperty("target", "")).toLowerCase(Locale.ROOT);
        if (target.contains("1.8") || target.contains("forge")) return true;
        try (JarFile jar = new JarFile(file.toFile())) {
            return jar.stream().map(JarEntry::getName).anyMatch(name ->
                    name.startsWith("net/minecraftforge/") || name.contains("func_") || name.contains("field_71439_g"));
        } catch (Exception ignored) {
            return true;
        }
    }

    private static boolean isCandidate(Path path) {
        try {
            if (!Files.isRegularFile(path) || Files.isSymbolicLink(path)) return false;
            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            return name.endsWith(".meowtils");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static final class LoadTransaction {
        final List<Module> modules = new ArrayList<>();
        final List<Object> listeners = new ArrayList<>();
    }
}
