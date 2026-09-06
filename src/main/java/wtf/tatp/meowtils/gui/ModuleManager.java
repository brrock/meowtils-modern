package wtf.tatp.meowtils.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import wtf.tatp.meowtils.event.api.EventManager;

public final class ModuleManager {
    private static final List<Module> modules = new CopyOnWriteArrayList<>();
    private ModuleManager() {}
    public static void register(Module... additions) {
        for (Module module : additions) {
            if (module == null || modules.stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(module.getName())))
                throw new IllegalStateException("Duplicate or null module: " + module);
            module.syncLegacyStateFromFields();
            modules.add(module);
            if (module.getState() && !module.alwaysEnabled) EventManager.register(module);
        }
        modules.sort(java.util.Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
    }
    public static void unregister(Module module) { EventManager.unregister(module); modules.remove(module); }
    public static List<Module> getModules() { return List.copyOf(modules); }
    public static Module find(String name) {
        return modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
    public static <T extends Module> T get(Class<T> type) { return Module.get(type); }
}
