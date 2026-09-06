package wtf.tatp.meowtils.manager.lists;

import java.nio.file.Path;
import java.util.Set;
import wtf.tatp.meowtils.MeowtilsData;

public final class FriendlistManager {
    private static final Path FILE = PlayerListStore.resolve(MeowtilsData.friendlist());
    private static Set<String> friendlist = PlayerListStore.loadSet(FILE);
    private FriendlistManager() {}
    public static void add(String uuidOrName) { friendlist.add(uuidOrName); PlayerListStore.save(FILE, friendlist); }
    public static void remove(String uuidOrName) { friendlist.remove(uuidOrName); PlayerListStore.save(FILE, friendlist); }
    public static boolean isFriendlisted(String uuidOrName) { return uuidOrName != null && friendlist.contains(uuidOrName); }
    public static Set<String> all() { return Set.copyOf(friendlist); }
}
