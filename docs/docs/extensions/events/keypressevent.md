# KeyPressEvent

Posted when a registered `KeyMapping` is pressed (Meowtils keybinds).

**Cancellable:** no

```java
@EventTarget
public void onKey(KeyPressEvent event) {
    String id = event.getId();
    KeyMapping mapping = event.getMapping();
}
```

This replaces the 1.8.9 `KeyInputEvent` (raw key code) page.
