package wtf.tatp.meowtils.manager.lists;

import java.nio.file.Path;
import java.util.Set;
import wtf.tatp.meowtils.MeowtilsData;

public final class SafelistManager {
    private static final Path FILE = PlayerListStore.resolve(MeowtilsData.safelist(), MeowtilsData.legacySafelist());
    private static Set<String> safelist = PlayerListStore.loadSet(FILE);
    private SafelistManager() {}
    public static void add(String uuidOrName) { safelist.add(uuidOrName); PlayerListStore.save(FILE, safelist); }
    public static void remove(String uuidOrName) { safelist.remove(uuidOrName); PlayerListStore.save(FILE, safelist); }
    public static boolean isSafelisted(String uuidOrName) { return uuidOrName != null && safelist.contains(uuidOrName); }
    public static Set<String> all() { return Set.copyOf(safelist); }
}
