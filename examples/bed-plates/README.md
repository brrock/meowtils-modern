# BedPlates

Minecraft 26.2 / Java 25 source port of `BedPlates-1.0.0.meowtils`, originally by **curxxed**. Recovered from the user-supplied archive; original behavior, UI layout constants and bundled assets are retained where applicable.

Original defense-layer scanning and the same slate rounded plates with inventory item icons. Empty beds stay hidden, same as 1.0.0. Disabled by default; disable Bedwars only to test in a local world.

## Build

From the repository root with JDK 25:

```sh
./gradlew :bed-plates:meowtilsArchive
```

Output: `examples/bed-plates/build/libs/bed-plates.meowtils`.

## Install

Build/install the base Meowtils mod from this checkout, then copy this archive into `<minecraft>/meowtils/extensions/`. Restart after updating the base mod; use `/reload` for extension-only changes. Remove the corresponding old archive first.

Rendering support is built into `wtf.tatp.meowtils.extension.render`; no support archive is needed. Fonts and textures remain in the extension. See the [extension docs](../../docs/docs/extensions/examples.md) and [custom UI SDK](../../docs/docs/extensions/render/custom-ui.md).

## Validation

Compile/package and SDK regression tests are automated. Plate chrome matches the original 1.0.0 layout and rounded-rect path. Item icons use Minecraft 26.2 inventory models.
