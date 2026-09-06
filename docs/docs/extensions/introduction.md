# Extensions

An **extension** is a zip of compiled classes with the `.meowtils` suffix. Meowtils loads it from `<minecraft>/meowtils/extensions/` and shows its modules under the **Extensions** category.

!!! Danger

    Extensions run with the same permissions as Minecraft. Only load archives you trust.

You should already be comfortable with Java 25 and the Minecraft 26.2 named APIs.

## Why extensions?

- Load and `/reload` without restarting the client
- Settings, config, and the Click GUI are already there
- No Fabric mod id — the archive is not a separate Fabric mod
- Same event bus as built-in modules

This is **not** a Forge or Lunar API. Old 1.8.9 `.meowtils` jars are refused. Rebuild against this 26.2 SDK.

## Install an extension

1. Put `something.meowtils` in `meowtils/extensions/` (or `/extensions` in chat).
2. Launch, or run `/reload` if the game is already open.

Loose `.jar` files and symlinks are ignored. The archive must contain `META-INF/meowtils.extension`.

## Standards

One feature per archive is easier to debug. Several modules in one archive still work.

## What is the file?

A zip of `.class` files, same as a jar, with a different extension. Rename to `.jar` if a decompiler requires that suffix.

A complete Gradle example lives in the repo at [`examples/hello-extension`](https://github.com/brrock/meowtils-modern/tree/main/examples/hello-extension).
