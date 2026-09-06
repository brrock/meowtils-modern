# Basic Usage

## GUI

### Bind

The default bind is **Right Shift**. Change it in the [GUI module](../modules/meowtils/gui.md), with `/bind`, or open the GUI once with `/meowtilsgui`.

### Categories

The **Meowtils** category holds always-on settings (GUI, Settings, Notifications, Teams, Icons). Other categories are Hypixel, Skywars, Bedwars, Render, Antisnipe, Utility, Advanced, and Extensions.

- **Right-click** a category header to open or close it.
- Drag a header to move the frame.

### Modules

- **Left-click** a module to toggle it.
- **Right-click** a module to expand its settings. Left-click a setting to change it.
- **Middle-click** a module to bind a key. Backspace clears the bind.
- Scroll over a long category to reach settings further down.

## HUD editor

Open the Click GUI, then click **HUD Editor** in the bottom-left. Only enabled modules that expose a HUD entry show a preview. Drag those previews to move them.

## Commands

`/meow` lists commands (7 pages). There is no `/meowtils` command tree.

If a client command shadows a server command, Meowtils still sends `/wdr`, `/report`, and similar names as server packets when they are forwarded from clickable chat. For a raw server send of something else, type the Hypixel command directly when it is not registered locally.

## Data folder

Config, lists, extensions, and auto-update files live in `<minecraft>/meowtils/`. Open it with `/meowtilsfolder`.
