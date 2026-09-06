# Hello Extension

A complete **Minecraft 26.2** Meowtils extension: settings, `/hello`, optional repeating chat, a chat trigger, and a HUD line.

## Build

From the repository root (JDK 25):

```sh
./gradlew :hello-extension:meowtilsArchive
```

Output: `examples/hello-extension/build/libs/hello-extension.meowtils`

## Install

Copy that file to `<minecraft>/meowtils/extensions/` and run `/reload` (or restart).

Enable **Hello Extension** in the Extensions category. `/hello` prints a prefixed line. Settings:

- **Greet on enable**
- **Show HUD** — version string at the top-left when no screen is open
- **Repeat seconds** — `0` disables the timer
- **Trigger word** — local reply when that word appears in chat

## Layout

```text
src/main/java/example/HelloExtension.java
src/main/resources/META-INF/meowtils.extension
```

`public static void init()` is required. Old 1.8.9 / Forge archives are refused.
