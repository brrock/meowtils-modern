# Module Setup

Extend `wtf.tatp.meowtils.extension.Extension`. Modules appear under **Extensions**.

You do **not** need `@Config public boolean enabled` or `@Config public int key`. `Module` already stores state and bind. Adding those fields is optional for source ports from 2.0.1.

```java
public final class HelloExtension extends Extension {
    private HelloExtension() {
        super("Hello Extension", "Your name");
        toggle("Greet on enable", "greet");
        info("Says hello from a 26.2 extension.");
    }

    public static void init() {
        Extension.registerModule(new HelloExtension());
    }
}
```

`info(...)` sets the tooltip and appends `Author: ...` when the author string is not null. Pass `null` as the author if you do not want that line.

See [`examples/hello-extension`](https://github.com/brrock/meowtils-modern/tree/main/examples/hello-extension).
