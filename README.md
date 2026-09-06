# Meowtils Modern

<img width="1905" height="1043" alt="image" src="https://github.com/user-attachments/assets/44f371bc-3a1b-495b-9f84-d7d963c620bb" />

Client-side [Fabric](https://fabricmc.net/) port of Meowtils 2.0.1 for **Minecraft 26.2**.
[Docs](https://brrock.github.io/meowtils-modern/) skidded ofc
## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.154.0+26.2 or newer
- Java 25 at runtime 

For 1.8.9-style servers such as Hypixel, use a **server-side** ViaVersion / ViaProxy path. Do not use ViaFabricPlus on the client.

## Install

1. Download `meowtils-<version>.jar` from [Releases](https://github.com/brrock/meowtils-modern/releases/latest).
2. Put it in the instance `mods` folder with Fabric Loader and Fabric API.
3. Launch once. Meowtils creates `<minecraft>/meowtils/` on start.
4. Open the Click GUI with **Right Shift** (rebind with middle-click on the GUI module, or `/bind`).

## Auto-update

Settings → **Auto-Updates** is on by default.

On join, Meowtils checks the latest GitHub release for `brrock/meowtils-modern`. If a newer version exists it downloads the jar into `meowtils/auto_update/` and swaps it in when the game exits. Turn the toggle off to get a clickable download link instead.

The first update also fetches `MeowtilsAutoUpdate.jar` from the same release. Restart after an update so the new jar loads.

## Use

`/meow` is the help index (7 pages). Common commands:

| Command | What it does |
| --- | --- |
| `/meow [page]` | Help |
| `/meowtilsgui` | Open the Click GUI |
| `/meowapi <key>` | Hypixel API key for Stats / SniperWarning |
| `/s <bw\|sw> <ign>` | Player stats |
| `/urchin <player>` | Urchin lookup |
| `/reload` | Reload `.meowtils` extensions |
| `/theme` | Prefix colors |
| `/anticheat` | AntiCheat alert colors |
| `/nickbot` | NickBot help |
| `/rq` | Requeue last Hypixel game |
| `/playcommands` | Short `/play` aliases (`/4s`, `/sw`, …) |
| `/shortcuts` | Party / chat / status shortcuts |
| `/meowfriend <player>` | Friend list (`/mf`, `/muf`) |
| `/blacklist` `/safelist` | Player lists |
| `/meowtilsfolder` | Open the data folder |

Clickable `[WDR]` on AntiCheat flags reports on the server. It does not re-enter the client `/wdr` handler.

## Data folder

Created under `<minecraft>/meowtils/`:

```text
meowtils/
  extensions/
  custom_cape/
  custom_skins/
  items/
  auto_update/
  chatfilters/
  config.json
  meowtils.log
  meowtilssafelist.json
  meowtilsblacklist.json
  meowtilsfriendlist.json
  urchintags.json
  nickbot_list.json
  autogg_list.json
  autogl_list.json
  items/itemhighlightblacklist.json
  items/itemhighlightsafelist.json
  chatfilters/default.txt
```

If the original file is missing, these legacy names are still read: `capes/`, `safelist.json`, `blacklist.json`, `autogg.json`, `autogl.json`, `itemhighlight_blacklist.json`, `itemhighlight_safelist.json`.

`nickbot_list.json` should be a JSON array (`[]`), not `{}`.

## Status

All 79 original `RegisterModule` names are native and enableable. That is not a claim of pixel-perfect 2.0.1 parity. Hypixel through ViaVersion still needs live checks.

Module-by-module notes live in [`PORT_STATUS.md`](PORT_STATUS.md).

## Build

JDK 25 is required.

```sh
./gradlew build
```

The mod jar is `build/libs/meowtils-<version>.jar`. The swap helper is `build/libs/MeowtilsAutoUpdate.jar`.

Tests, build, and install into the Prism `idk meowtils test` instance:

```sh
./build.sh
```

`JAVA_HOME` is used when it already points at JDK 25; otherwise the script uses Temurin 25. Previous jars are moved to `minecraft/meowtils/build-backups/`. Restart the instance after install.

```sh
./build.sh "/path/to/PrismLauncher/instances/My Instance"
```

## Release

Publishing is a manual GitHub Action on [brrock/meowtils-modern](https://github.com/brrock/meowtils-modern).

1. Open **Actions → Release → Run workflow**.
2. Pick the bump:
   - **patch** `0.0.0` → `0.0.1`
   - **minor** `0.0.0` → `0.1.0`
   - **major** `0.0.0` → `1.0.0`
3. The workflow updates `mod_version` in `gradle.properties`, commits, tags `v<version>`, builds, and publishes the GitHub release with both jars.

Clients with Auto-Updates on pick that release up on the next join.

## Extensions

Client-side **`.meowtils`** archives only (loose `.jar` files and symlinks are ignored):

```text
<minecraft>/meowtils/extensions/
```

Each archive needs `META-INF/meowtils.extension` and a `public static void init()` entry:

```properties
main=example.MyExtension
```

```java
public final class MyExtension extends Extension {
    private static MyExtension instance;

    private MyExtension() {
        super("My extension", "Your name");
        toggle("Enabled feature", "enabled");
    }

    public static void init() {
        instance = new MyExtension();
        Extension.registerModule(instance);
    }
}
```

Zip-slip entries, invalid main class names, and 1.8.9 / Forge binaries are refused. Reload with `/reload` (aliases `reloadextensions`, `reloadextension`).

Kept from Meowtils 2.x:

- `Extension` / `Module` lifecycle: `onEnable`, `onDisable`, `onReset`, `setState`
- GUI values: `toggle`, `check`, `slider`, `mode`, `text`, `opacity`, `bind`, `button`, `expand`
- `get()`, `set(...)`, `getValue()`
- `@EventTarget`, `EventManager`, priorities, cancellable events
- `KeybindManager.register(...)` and `KeyPressEvent`
- `getResource(...)`, `extension.resource(...)`, `extension.openResource(...)`
- `HttpJson.get(...)` for non-blocking JSON
- `Meowtils.addMessage(...)` for prefixed chat
- `RenderWorldLastEvent` (`PoseStack`, `SubmitNodeCollector`)
- `WorldRenderEvent.submitOutline(...)` for 26.2 shape outlines

Old `.meowtils` sources can be ported; old binaries cannot. They link 1.8.9 classes and must be recompiled against this 26.2 SDK:

1. Replace 1.8.9 Minecraft calls with 26.2 named APIs.
2. Keep setup in an `Extension` subclass.
3. Swap old events for Fabric callbacks or current Meowtils events.
4. Move raw OpenGL to Blaze3D / Fabric rendering.
5. Package classes and resources with `META-INF/meowtils.extension`.

A working Gradle example is [`examples/hello-extension`](examples/hello-extension). From the repo root:

```sh
./gradlew :hello-extension:meowtilsArchive
```

Copy `examples/hello-extension/build/libs/hello-extension.meowtils` into `meowtils/extensions/` and `/reload`.

## Docs

The site is built from [`docs/`](docs/) with MkDocs and published by GitHub Pages:

[https://brrock.github.io/meowtils-modern/](https://brrock.github.io/meowtils-modern/)

```sh
cd docs
pip install -r requirements.txt
mkdocs serve
```

## License

MIT. Original Meowtils and docs by femboytatp. This 26.2 port is [brrock/meowtils-modern](https://github.com/brrock/meowtils-modern) and by brrock. 

## AIs used: 
- For notifications and clickgui porting GPT6 Astra, (it was the only model that got it looking like the 1.8.9 version)
- Rest of the modules and lifecycle was done by Grok 4.6 High Fast (i remebered I had a cursor sub and wanted to use it lol)
This took about 3 hours in total with testing, due grok's fast mode and subagents (i had 12 in paralell at times)
