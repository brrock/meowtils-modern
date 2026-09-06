# Events

Hook game and Meowtils callbacks with `@EventTarget` on a `void` method that takes one event type.

This is not Forge. Do not use `@SubscribeEvent`.

## Register

`setState(true)` registers the module on the event bus. Extra classes need `Extension.registerEvent(listener)` in `init()`.

```java
Extension.registerEvent(new ExtraHandler());
```

## Annotation

```java
@EventTarget(priority = EventPriority.HIGHEST)
public void onClientTick(ClientTickEvent event) { }
```

Priorities: `HIGHEST`, `HIGH`, `NORMAL`, `LOW`, `LOWEST`. Default is `NORMAL`.

## Phase

`ClientTickEvent` and `RenderTickEvent` have `PRE` and `POST`. Use **POST** unless you have a reason not to.

`ChatReceivedEvent` uses `isOverlay()` instead of a chat type enum.

## Cancel

```java
event.setCancelled(true);
```

Only cancel when you know the side effect (for example hiding a chat line).

## 26.2 names

| Use this | Not this |
| --- | --- |
| `mc.player` | `mc.thePlayer` |
| `mc.level` | `mc.theWorld` |
| `mc.gui.screen()` | `mc.currentScreen` |
| `Minecraft.getInstance()` | `Minecraft.getMinecraft()` |
| `KeyPressEvent` | `KeyInputEvent` |

These events from 2.0.1 are **not** posted: `EntityJoinWorldEvent`, `RenderPlayerEvent`, `MovementInputUpdateEvent`. Use `WorldEvent`, packets, or a Fabric callback if you need that hook.

```java
@EventTarget
public void onClientTick(ClientTickEvent event) {
    if (mc.player == null || mc.level == null) return;
    if (event.getPhase() != ClientTickEvent.Phase.POST) return;
    Meowtils.addMessage("Client tick!");
}
```
