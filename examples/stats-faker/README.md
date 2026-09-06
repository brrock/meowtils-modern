# StatsFaker

Minecraft 26.2 / Java 25 source port of `StatsFaker-1.1.meowtils`, originally by **Mega**. Recovered from the user-supplied archive; original behavior, UI layout constants and bundled assets are retained where applicable.

Original local Bedwars and network text replacement, Set/Add modes and settings. Disabled by default. Server stats are unaffected.

## Build

From the repository root with JDK 25:

```sh
./gradlew :stats-faker:meowtilsArchive
```

Output: `examples/stats-faker/build/libs/stats-faker.meowtils`.

## Install

Build/install the base Meowtils mod from this checkout, then copy this archive into `<minecraft>/meowtils/extensions/`. Restart after updating the base mod; use `/reload` for extension-only changes. Remove the corresponding old archive first.

Rendering support is built into `wtf.tatp.meowtils.extension.render`; no support archive is needed. Fonts and textures remain in the extension. See the [extension docs](../../docs/docs/extensions/examples.md) and [custom UI SDK](../../docs/docs/extensions/render/custom-ui.md).

## Validation

Compile/package and SDK regression tests are automated. Exact visual parity with the legacy client has not been verified; current Minecraft item models and native blur are used.
