# Entrypoint

`public static void init()` on the class named in `META-INF/meowtils.extension` runs once when the archive loads.

```java
public final class HelloExtension {
    public static void init() {
        HelloExtension module = new HelloExtension();
        Extension.registerModule(module);
        Extension.registerEvent(module);
        Extension.registerCommand(/* brigadier literal */);
    }
}
```

## Register

- **Modules** — `Extension.registerModule(module)`
- **Extra event listeners** — `Extension.registerEvent(listener)` (the module itself is registered when you enable it; register it in `init` if you need events while writing the module as the listener immediately)
- **Commands** — `Extension.registerCommand(LiteralArgumentBuilder)` or `CommandManager.register(...)`

`CommandHandler`, `SlotManager`, and the old 1.8 icon registrar are gone. Name icons go through `IconManager` / `IconProvider` if you add one in Java; there is no separate `CommandHandler` class.
