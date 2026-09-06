# Example extensions

The repository includes five independently packaged Java 25 / Minecraft 26.2 extensions:

| Gradle project / folder under `examples` | Module | Features |
| --- | --- | --- |
| `hello-extension` | Hello Extension | Basic settings, command, chat and HUD example |
| `tenacity-gui` | Tenacity GUI | Original floating panels, themes, search, controls, animations and bundled fonts |
| `notifications` | Notifications+ | Tenacity 5.0 and Vape V4 notification styles, offset and test button |
| `bed-plates` | BedPlates | Projected bed-defense item plates, distance scaling, opacity and display binds |
| `stats-faker` | StatsFaker | Local Bedwars/network display changes, Set/Add mode and original settings |

The last four are source ports of the supplied TenacityGUI 1.2, Notifications 1.1, BedPlates 1.0.0 and StatsFaker 1.1 archives. Their original UI assets and layout constants are retained. Minecraft's current item models and native blur implementation are used by the modern renderer.

## Build

From the repository root with JDK 25:

```sh
./gradlew :tenacity-gui:meowtilsArchive :notifications:meowtilsArchive \
  :bed-plates:meowtilsArchive :stats-faker:meowtilsArchive
```

Each output is `examples/<project>/build/libs/<project>.meowtils`. `./gradlew build` also builds every example. GitHub releases attach the same archives next to the mod jar.

## Install

Use the Meowtils build from the same checkout: these examples depend on its shared rendering support and notification/text events. Install that mod normally, copy the desired `.meowtils` files into `<minecraft>/meowtils/extensions/`, then restart Minecraft for an updated base mod. Subsequent extension-only changes can use `/reload`.

No support archive is required. Remove the corresponding old 1.8.9 extension archives from that directory before installing these ports.

Tenacity GUI and Notifications+ default to enabled; BedPlates and StatsFaker default to disabled. Open the normal Meowtils GUI keybind to use Tenacity. Its appearance settings are in the GUI module. Panel positions and appearance also persist in `meowtils/TenacityGUI/config.json`.

BedPlates defaults to Bedwars only. Disable that option when testing in a local world. StatsFaker changes rendered text locally; it does not change server-side stats.
