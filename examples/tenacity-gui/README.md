# Tenacity GUI

Minecraft 26.2 / Java 25 source port of `TenacityGUI-1.2.meowtils`, originally by **Mega**. Recovered from the user-supplied archive; original behavior, UI layout constants and bundled assets are retained where applicable.

Floating panels, themes, search, original setting widgets and animations. Open using the normal Meowtils GUI bind. Appearance controls remain in the GUI module.

## Build

From the repository root with JDK 25:

```sh
./gradlew :tenacity-gui:meowtilsArchive
```

Output: `examples/tenacity-gui/build/libs/tenacity-gui.meowtils`.

## Install

Build/install the base Meowtils mod from this checkout, then copy this archive into `<minecraft>/meowtils/extensions/`. Restart after updating the base mod; use `/reload` for extension-only changes. Remove the corresponding old archive first.

Rendering support is built into `wtf.tatp.meowtils.extension.render`; no support archive is needed. Fonts and textures remain in the extension. See the [extension docs](../../docs/docs/extensions/examples.md) and [custom UI SDK](../../docs/docs/extensions/render/custom-ui.md).

## Validation

Compile/package and SDK regression tests are automated. Exact visual parity with the legacy client has not been verified; current Minecraft item models and native blur are used.
