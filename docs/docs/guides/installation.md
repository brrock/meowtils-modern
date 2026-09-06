# Installation

Meowtils is a normal Fabric client mod. Drop the jar in `mods` once. Later versions are handled by [Auto-Updates](../other/auto-updates.md) unless you turn that off.

---

## 1. Install Fabric 26.2

1. Install [Prism Launcher](https://prismlauncher.org/) (or another launcher that can run Fabric 26.2).
2. Create an instance for **Minecraft 26.2**.
3. Add **Fabric Loader 0.19.3 or newer**.
4. Add **Fabric API 0.154.0+26.2 or newer**.
5. Set the instance Java to **Java 25**. Prism’s bundled Java 25 runtime is fine.

!!! Warning "Hypixel / 1.8.9 servers"

    Use **server-side** ViaVersion or ViaProxy. Do not install ViaFabricPlus on this client.

## 2. Add Meowtils

1. Download `meowtils-<version>.jar` from [Releases](https://github.com/brrock/meowtils-modern/releases/latest).
2. Put it in the instance `mods` folder next to Fabric API.

You do not need `MeowtilsAutoUpdate.jar` in `mods`. Auto-update downloads that helper into `meowtils/auto_update/` when needed.

## 3. Launch

Launch the instance. Meowtils creates `<minecraft>/meowtils/` on first start.

Open the Click GUI with **Right Shift**. If that does nothing, use `/meowtilsgui` and rebind in the GUI module.

Then read [Basic Usage](basic-usage.md).

---

## Uninstall

Remove `meowtils-*.jar` from `mods`. The `meowtils/` data folder can stay if you want to keep config and lists.
