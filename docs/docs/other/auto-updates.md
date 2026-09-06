# Auto Updates

Meowtils checks [brrock/meowtils-modern](https://github.com/brrock/meowtils-modern) for a newer GitHub release when you first join a world.

Turn this off in [Settings](../modules/meowtils/settings.md) if you want to update by hand.

!!! Tip

    Keep auto-updates on, or install each release yourself. Old builds miss fixes.

## When enabled

If the installed version is older than the latest release tag, Meowtils downloads `meowtils-<version>.jar` into `meowtils/auto_update/`. Closing the game runs `MeowtilsAutoUpdate.jar`, which replaces the jar in `mods`. Launch again to load the new build.

`MeowtilsAutoUpdate.jar` is also taken from the same GitHub release, and only if it is missing.

## When disabled

Chat still tells you a newer release exists and gives a clickable download link. You replace the jar in `mods` yourself.

## Versioning

This port starts at `0.0.0`. Releases are published with the manual **Release** workflow on GitHub (`patch` / `minor` / `major`). The first published tag is `v0.0.1`, `v0.1.0`, or `v1.0.0` depending on the bump you pick.
