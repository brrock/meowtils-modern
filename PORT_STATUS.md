# Meowtils GUI port status

This is an implementation and verification ledger, **not a claim of full parity**. Target: Minecraft 26.2, Fabric Loader 0.19.3+, Java 25. No mappings dependency was added.

## Authority and extraction

## Protocol compatibility requirement

The client targets Minecraft 26.2, but module behavior must also remain correct when a server uses ViaVersion or a similar protocol translator to present a 1.8.9-style server environment (including Hypixel). Module ports therefore must be validated against both modern client APIs and legacy-server semantics: packet names and fields may be translated, scoreboard/chat text may use legacy formatting, inventory and item identifiers must be recognized across protocol versions, and timing-sensitive features must use client ticks rather than assuming a 26.2 server. A module is not considered fully ported while it only works on a native 26.2 server. Each checklist entry below should record any ViaVersion-specific limitation and test evidence.

The visual/behavioral references are the local decompilation of Meowtils 2.0.1 at `/tmp/meowtils-jadx.RjKv5u/sources` and `/tmp/meowtils-inspect/Meowtils-2.0.1.jar`. Original GUI PNGs and the TTF are reused. `tools/extract-original-settings.mjs` reads the original RegisterModule and constructors; `src/main/resources/meowtils-original-settings.json` records all 79 original modules and 577 controls including nested controls, source hashes, original defaults, config IDs, ordering, ranges, modes, tooltips and action source.

Run the extractor with the source directory argument to reproduce its JSON on stdout. The original null text prefixes are preserved in that data and normalized to empty labels when controls are constructed.

## GUI fidelity and verification

| Area | Implemented | Verification / limitation |
| --- | --- | --- |
| Categories | Original enum order, default positions, alphabetical modules; empty Extensions frame hidden | Source comparison; default frames are closed as in the original |
| Geometry | 90×20 texture rectangles, 11-unit header offset, 15-unit module rows, 12-unit controls, original margins | Source-derived; pixel comparison still required |
| Assets/colors | Original category/module/control/icon textures; RGB accent defaults 189/140/255 and linked HSB controls | Assets copied from release; backend filtering/rounding may differ |
| Font | Original TTF and original AWT atlas bake: 30px source size, fractional advances, glyph padding, .4/.1-unit shadows | Antialiasing/metrics regression test; linear GPU sampling. Final pixel comparison still required |
| Scale | Tiny .8, Small .9, Normal 1, Large 1.1, Huge 1.2, Auto framebufferWidth/1920, adjusted for Minecraft GUI scale | Unit-tested math; all scales still need in-game visual inspection |
| Input | Right Shift by default; header drag/right-click collapse; module toggle/right-click settings/middle-click binding | Compiles; live input workflows still require verification |
| Settings | Toggle, check, mode dropdown, slider, text/paste/backspace, bind, hue/saturation/brightness/opacity, nested expand, action | Unit tests cover track math, nested visibility, text limits/Unicode, shared color state; screen hit targets not yet exhaustively exercised |
| Dropdowns | Foreground layer, selected mode excluded from options, outside click closes | Source-derived; clipping and display-edge behavior still need visual checks |
| Tooltips | 250ms delay, mouse +6 offset, half GUI scale, wrapped lines, dark background | Source-derived; formatting and edge placement remain approximate |
| Scroll | Whole-frame scroll with original speed option and one-unit-per-render smoothing | Source-derived; very short windows and all long lists need in-game checks |
| Persistence | Frame positions/open state, module binding, primitive values, nested values, color state; atomic `config.json` replacement | Round-trip test covers real fields, integer binding, nested text and frames. Saved on GUI/HUD close, keybind toggle, disconnect, and client stop |
| HUD editor | Bottom-left entry button; enabled native entries, hover bounds/name, dragging and saved fields; editor background blur disabled | Original HUD modules now expose `hudEditor()` where the 2.0.1 module was draggable; live bounds still need in-game checks |
| Compatibility | Existing modules/extensions/events/commands/resources retained; recursive values and legacy field binding; HudEntry and GuiUtil aliases | Existing API tests pass; **no binary compatibility with Forge 1.8.9 extensions** |
| Lifecycle | `MeowtilsData.ensure()` on start and stop creates the original `meowtils/` layout; native register → handlers → `.meowtils` only → config load; world load/unload resets; stop saves then `ExtensionManager.shutdown()` | Loader refuses symlinks, zip-slip, invalid `main`, missing `public static void init()`, and 1.8.9/Forge archives. Loose `.jar` files are not loaded |
| Background | Click GUI uses one 26.2 blur stratum when enabled; HUD editor bypasses menu blur so editable overlays remain sharp | Uses the modern blur implementation, not the old Forge shader |
| Runtime | Null-label startup crash fixed | Updated jar passed initialization and resource/renderer loading in the user's Prism 0.19.3 / Fabric API 0.154.0 instance on 2026-09-05 |

The computer-use tool can inspect Prism Launcher but does not expose the launched Java game as a target. Live visual/input verification has not been completed. In particular, this ledger does not certify every workflow or full pixel parity.

## Client lifecycle

Startup order in `MeowtilsClient`:

1. `MeowtilsData.ensure()` creates the original game-directory layout: `meowtils/{extensions,custom_cape,custom_skins,items,auto_update,chatfilters}` plus `config.json`, `meowtils.log`, `meowtilssafelist.json`, `meowtilsblacklist.json`, `meowtilsfriendlist.json`, `urchintags.json`, `nickbot_list.json`, `autogg_list.json`, `autogl_list.json`, `items/itemhighlightblacklist.json`, `items/itemhighlightsafelist.json`, and `chatfilters/default.txt`.
2. Register the 79 original modules only. Extensions stay in `meowtils/extensions`.
3. `OriginalModuleSettings.install()` restores 2.0.1 controls onto those instances. Every registered name has a native class and is enableable (`behaviorAvailable=true`). Only a missing name would become an activation-refused stub via `setPortStatus(..., false)`.
4. Register always-on handlers: notifications, session/scoreboard, TeamUtil, PartyHandler, ResetHandler, icons, NickBot.
5. Load `minecraft/meowtils/extensions/*.meowtils` (and `.jar` files that carry `META-INF/meowtils.extension`). Legacy 1.8.9/Forge archives are refused, not half-loaded.
6. Load `config.json` onto native + extension modules.

Runtime: client ticks post PRE/POST `ClientTickEvent` and `RenderTickEvent`; chat, HUD, world-last, attack, and block/item use events are posted. World LOAD/UNLOAD reset session flags, party state, bot tab cache, and each module’s `onReset()`. Config is written atomically on Click GUI / HUD editor close, module keybind toggle, disconnect, and client stop.

Original commands (2.0.1): help is `/meow`; extension reload is `/reload` (aliases `reloadextensions`, `reloadextension`); Hypixel API key is `/meowapi <key>` (aliases `meowapikey`, `meowtilsapi`). The invented `/meowtils` tree is gone.

## Native module checklist

Every original RegisterModule name has a native class constructed in `RegisterModule.java` and is enableable. Status is **not** full 2.0.1 parity: ViaVersion/Hypixel live checks are still required. **Implemented** means the core original loop exists in events, mixins, or HUD. **Partial** means a native class with real logic plus a known gap (outlines instead of fill, missing friends, 1.8 blocking approximated, unverified ViaVersion paths, unwired overlay hooks). **Unavailable** would mean `setPortStatus(..., false)` / activation refused; none of the 79 names are in that state. Existing modern-only settings stay under “Fabric options.”

| Module | Category | Original controls (including nested) | Behavior status |
| --- | --- | ---: | --- |
| AccountHider | Hypixel | 8 | Implemented: `/customname`, tab rewrite, local nametag, and first-person arm via `texturePath`/`getSkin`. ViaVersion tab text still needs live checks. |
| ActionSounds | Utility | 3 | Implemented: crit sound plus blocked-damage anvil on hurt/damage packets while blocking or 1.8 sword-blocking. ViaVersion hurt packets remain required. |
| Animations | Render | 6 | Implemented: cancel swing/consume/bow, fake autoblock as `ItemUseAnimation.BLOCK`, and LocalPlayer use-ticks/`isBlocking` for ViaVersion 1.8 use-item swords. Live 1.8-via pose still required. |
| Anti-Invis | Render | 1 | Implemented: reveal non-bots and apply the opacity slider through the 26.2 translucent model tint. Live alpha still needs visual checks. |
| AntiCheat | Antisnipe | 7 | Implemented: tick checks AutoBlock/NoSlow/Killaura/Legit Scaffold, violation levels, WDR button with `Prefix.getPrefix()`, `/anticheat` color picker (client-side clicks), AutoBlacklist hook, PartyNotifier, and friend skip via `TeamUtil.ignoreFriends`. Clicking `[WDR]` now sends a server command packet so Fabric does not re-enter the client `/wdr` handler. ViaVersion movement still required. |
| AntiMisplace | Bedwars | 0 | Implemented: cancels obsidian place when the target is not adjacent to a bed and notifies. ViaVersion validation remains required. |
| AntiObfuscate | Utility | 0 | Implemented: strips `§k` on `RenderStringEvent` and drops obfuscated `Style` on `FormattedCharSequence` prepare/width. |
| ArmorAlerts | Bedwars | 5 | Implemented: 20-tick scan of enemy leggings for chain/iron/diamond with chat/notification/sound. ViaVersion item IDs remain required. |
| AutoBlacklist | Antisnipe | 11 | Implemented: blacklists on `/wdr`/`/report` packets and AntiCheat flags, with Mojang UUID lookup and JSON list. ViaVersion command packets remain required. |
| AutoChannel | Hypixel | 0 | Implemented: compatibility helpers switch between Hypixel `/chat all` and `/chat party` while enabled. ViaVersion validation remains required. |
| AutoChest | Advanced | 11 | Implemented: GuiOpen-only deposit start, dump/take binds, mouse-abort, clicked-slot overlay. Gates use `AutoChestGate` (blank/Via titles, I18n, typed ender) and reject shop screens. Resources use `ItemIds`/`resourceKey` so diamond swords are not taken. Via click timing still needs a live check. |
| AutoGG | Hypixel | 14 | Implemented: Hypixel/Universal gating, `HypixelUtil.GAME_END_MESSAGES` plus no-colon chat, `activated` reset on world load, Auto GL `The game starts in N second(s)!` line, DelayedTask `/ac` delays, random lists on `autogg_list.json`/`autogl_list.json` (legacy files still read), and original `/autogg`/`/autogl` strings. ViaVersion chat formatting remains required. |
| AutoSafelist | Antisnipe | 1 | Implemented: safelists the victim of a Bedwars `FINAL KILL!` line via Mojang UUID. ViaVersion chat formatting remains required. |
| AutoStairs | Advanced | 0 | Implemented: jumps after a 0.5-block stair step-up while sprinting. ViaVersion validation remains required. |
| AutoSwap | Advanced | 7 | Implemented: swaps to another hotbar stack when the selected item is used up; optional sword-on-attack. ViaVersion item IDs remain required. |
| AutoText | Utility | 21 | Implemented: ten independent bind slots fire together on the same tick, with original hold-repeat every client tick. |
| AutoTip | Hypixel | 2 | Implemented: `/tip all` on the delay slider and optional hide of tip replies. ViaVersion validation remains required. |
| AutoWho | Hypixel | 1 | Implemented: schedules `/who` after the original start message and optionally hides the response lines. ViaVersion validation remains required. |
| BedESP | Bedwars | 7 | Implemented: one-time 1.8-height (Y 0–255) section scan plus chunk-packet updates. Render keeps the last box through Via air/`PART` flicker; one canonical key per bed; prune only after 20 loaded-chunk misses. The 26.2 full-height 40-tick rescan is gone. ViaVersion block IDs remain required. |
| BedTracker | Bedwars | 8 | Implemented: locates a nearby bed after start/respawn chat, range/enemy alerts, and a draggable HUD. ViaVersion chat/range still required. |
| BlockCount | Render | 7 | Implemented: held-stack count, threshold color, and PING_DEEP alert. ViaVersion item IDs remain required. |
| BreakProgress | Render | 7 | Implemented: projects destroy-progress percentage or remaining seconds onto the looked-at block. ViaVersion break timing remains required. |
| Cape | Render | 4 | Implemented: `AvatarRendererMixin` + `CapeManager` apply selected/custom capes from `meowtils/custom_cape` (legacy `capes/` still read). Live first-person/elytra still need checks. |
| ChatFilter | Utility | 3 | Implemented: file-backed filter selection, original `?`/`=`/`<`/`>`/`!`/`&` syntax, `-ServerName` gating via scoreboard title/lines plus current server address, and GUI Open folder/Reload actions. ViaVersion sidebar text remains required. |
| ChestESP | Render | 8 | Implemented: unopened/opened chests from globally rendered block entities; `Full` is a translucent filled AABB. ViaVersion block entities remain required. |
| ConsumeAlerts | Bedwars | 9 | Implemented: tracks golden apple/milk/potion use start/stop and alerts with distance. ViaVersion use-item packets remain required. |
| ConsumeTimer | Render | 6 | Implemented: HUD ticks/seconds while a short-use item is consumed, with editor preview. ViaVersion use duration remains required. |
| CooldownHUD | Skywars | 5 | Implemented: Corrupt Pearl / Enderchest / End Lord / Ice Bridge / Echo kit timers from actionbar + ViaVersion lore. |
| DamageTags | Render | 8 | Implemented: floating damage numbers after a successful hit, with expire/fade/suffix. ViaVersion health sync remains required. |
| DelayRemover | Advanced | 11 | Implemented: mixin writes reduce break, use, miss, and jump delays from the restored sliders. Detectability and ViaVersion tick rates remain. |
| Denicker | Hypixel | 5 | Implemented: tab-list UUID-v1 nick check, textures-property JSON (`profileName` + skin hash), bundled nick hashes, chat/PartyNotifier, Stats auto-check. Empty Via `GameProfile` properties fall back to `PlayerInfo.getSkin()` URL hash (hash-only; real name still needs the property). No book denicker (not in 2.0.1). |
| EquipAlerts | Skywars | 6 | Implemented: alerts when an enemy equips diamond armor pieces. ViaVersion item IDs remain required. |
| EventTimers | Bedwars | 14 | Implemented: diamond/emerald/bed-gone/sudden-death/game-end HUD plus emerald spawn totals from the original schedule. Scoreboard/ViaVersion start timing still required. |
| Freelook | Utility | 5 | Implemented: hold/toggle bind, start perspective, optional FOV, camera mixins that do not change server yaw/pitch. Live input still needs checks. |
| GhostHand | Advanced | 19 | Implemented: `GhostHandPickMixin` lets the crosshair pass through configured players/armor stands when item/block whitelists match. ViaVersion hit results remain required. |
| GUI | Meowtils | 10 | Implemented custom frames and controls; visual equivalence remains unverified. |
| HealthDisplay | Render | 7 | Implemented: own-health overlay plus live bow damage (`getTicksUsingItem` + Power) and Hypixel `is on X HP!` for 2s. Precise original bounds still need visual checks. |
| HealthESP | Render | 1 | Implemented: original Raven world-space yaw-billboard health bars. Via/1.8 zero-health flashes keep the last ratio; bars sit on the interpolated box and offset toward the camera. |
| HeightOverlay | Bedwars | 7 | Implemented: start/respawn triggers send `/map`; overlay and normal chat both parse `You are currently playing on` (trim, trailing punctuation); HUD plus wool recolor at/above the limit. Map JSON lookup still needs a live Hypixel name check. |
| HotbarLock | Utility | 12 | Implemented: Manual slot locking and Swords drop protection through the client key pipeline; slot state is read from restored original settings. ViaVersion validation remains required. |
| Icons | Meowtils | 4 | Implemented: tab/nametag prefixes for nick, murderer, Skywars, friend, safelist, blacklist, Urchin, and Stats. |
| Indicators | Render | 9 | Implemented: 3D fill or outline plus existing 2D HUD boxes for arrows/fireballs/pearls. |
| InstantHurt | Render | 0 | Implemented: starts the client hurt overlay as soon as the local player attacks. ViaVersion hurt-time still required. |
| ItemAlerts | Bedwars | 26 | Implemented: held-item alerts for the original Bedwars/rotation set with cooldown, distance, and sound modes. ViaVersion item IDs remain required. |
| ItemESP | Render | 16 | Implemented: original world-space 3D/2D boxes and nametag labels (auto-scale is world-space, not HUD). Exact 2.0.1 iron/gold/diamond/emerald lists. Live 2D billboard still needs a visual check. |
| ItemHighlight | Skywars | 15 | Implemented: best-item scoring, JSON lists, `/itemsl`/`itembl`, slot overlay on ShinyPots, and throw/drop cancel. |
| ItemScale | Render | 5 | Implemented: `ItemEntityRendererMixin` scales dropped models from the slider and important-only filters. Live scale still needs checks. |
| LatencyAlerts | Utility | 3 | Implemented: always-on `LatencyHandler` packet clock, original 3s alert spacing, sidebar-null limbo skip, and Chat/Notification/All routing with original color strings. |
| MiningAlerts | Skywars | 2 | Implemented: alerts when a nearby player finishes mining diamond ore (`ClientboundBlockDestructionPacket` progress 9). ViaVersion packets remain required. |
| MurdererFinder | Hypixel | 7 | Implemented: role detection, chat alerts, 3D fill/outline, and 2D billboard. ViaVersion Murder Mystery still required. |
| NickBot | Hypixel | 11 | Implemented: `/nickbot` list/start, book-GUI reroll (`BookViewScreen` + held/lectern `BookAccess`), JSON/plain flatten, blank-line nick extract, overlay + chat nick-success lines, ESC stop, HUD, `nickbot_list.json` (`[]` or empty `{}`). Live Hypixel book layout still needs a visual check. |
| NoArmorDye | Skywars | 1 | Implemented: item/model mixins strip leather dye while Skywars is active. ViaVersion dye components remain required. |
| NoParticles | Utility | 3 | Implemented: restored glyph, sponge-cloud and block-break controls filter corresponding particle options. ViaVersion validation remains required. |
| Notifications | Meowtils | 8 | Implemented: start/layout, shout cooldown, challenge warning, banned-player scan, and blacklist join alerts. Live layout still needs visual checks. |
| NoTitles | Utility | 0 | Implemented: `HudMixin` cancels title and subtitle overlays while enabled. |
| NullMove | Utility | 0 | Implemented: original press-time ordering is applied in the 26.2 KeyMapping pipeline. ViaVersion validation remains required. |
| PartyDetector | Antisnipe | 6 | Implemented: clustered player-spawn packets in Bedwars pregame warn of party joins; optional missed-NPC count. ViaVersion spawn packets remain required. |
| PartyNotifier | Hypixel | 30 | Implemented: queued `/pc` templates for AntiCheat, Denicker, Bedwars alerts, PartyDetector, and Urchin. Live party-channel and ViaVersion still required. |
| PearlDetector | Skywars | 12 | Implemented: Time Warp HUD, thrown-pearl alerts, friend skip, and 4s boxes. ViaVersion lore/packets remain required. |
| PickupAlerts | Bedwars | 6 | Implemented: `ClientboundTakeItemEntityPacket` alerts for iron/gold/diamond/emerald with cooldown. ViaVersion packets remain required. |
| PingHUD | Render | 3 | Implemented: 60s `LatencyHandler.ping` plus original latency colors; singleplayer shows 0. |
| PotionHUD | Render | 15 | Implemented: original eight effects with checkboxes, infinite hide, expire chat, and PING_MEDIUM. |
| Requeue | Hypixel | 4 | Implemented: Hypixel-only `/locraw` after join/scoreboard, limbo/hub/replay/housing filters, JSON locraw hide, last `/play` save, feedback chat/notification, requeue bind, and delayed auto-requeue on `GAME_END_MESSAGES`. ViaVersion locraw still required. |
| ResourceTracker | Bedwars | 10 | Implemented: inventory + open ender-chest counts on HUD/chat, optional hide of `+ iron/gold/...` lines. ViaVersion chest titles remain required. |
| SessionStats | Hypixel | 18 | Implemented: Bedwars tab-footer kills/finals, chat XP/elimination, Skywars kill/XP, recap, and draggable HUD. ViaVersion tab footer still required. |
| Settings | Meowtils | 6 | Implemented: prefix/smooth-font/copy-chat (right/middle click and Ctrl+C), HUD HudFont when Smooth is on, `/theme` and `/anticheat` color clicks stay on the client. Auto-Updates checks `brrock/meowtils-modern` releases and swaps the jar on next launch. |
| ShinyPots | Render | 0 | Implemented: potion color behind inventory slots via `AbstractContainerScreenMixin`. |
| ShopHelper | Bedwars | 4 | Implemented: lore-cost highlight, middle-click replace, and duplicate sword/stick block on shop screens. ViaVersion shop lore remains required. |
| SkywarsAlerts | Skywars | 19 | Implemented: held-item alerts, icon cache, SkywarsIcon, and friend skip. ViaVersion lore still required. |
| SniperWarning | Antisnipe | 5 | Implemented: gear/name/stats heuristics, friend skip, and optional Hypixel fetch. Clutch/FKDR thresholds need live Hypixel checks. |
| Sprint | Utility | 0 | Implemented: keeps sprint while moving (optional omni under Fabric options). Movement parity unverified. |
| Stats | Hypixel | 33 | Implemented: cache/cooldown, ChatStats `/s` `/info` `/recent` `/playerstatus`, Urchin persist + `/urchin`, who-line auto-check, and tab/nametag icons. Local chat is not re-parsed. Tab Urchin fetches are skipped when no key is set (one warning, not a per-player log). Empty Via/Hypixel skin signatures are treated as unsigned so lobby NPCs do not RSA-fail. Live Hypixel/Urchin keys still required. |
| StrengthESP | Skywars | 6 | Implemented: Strength potion scan, 3D fill/outline, 2D boxes, and fade-out. ViaVersion effect sync remains required. |
| Teams | Meowtils | 3 | Implemented: bot/team/friend ignore via `FriendlistManager` (`meowtilsfriendlist.json`) and `/meowfriend`. |
| TimeChanger | Render | 4 | Implemented: client-side day-time override via `ClientClockManager.getTotalTicks` (26.2 replaced `Level.getDayTime`) and original 0–24 hour conversion, including real-time mode. ViaVersion validation remains required because the server clock is intentionally left untouched. |
| TrapNotifier | Bedwars | 3 | Implemented: trap-trigger alerts from effects/reveal chat and missing-trap reminder after 30s. ViaVersion chat remains required. |
| UpgradeAlerts | Bedwars | 2 | Implemented: first Sharpness/Protection sighting per enemy team. ViaVersion enchant sync remains required. |
| UpgradeHUD | Bedwars | 11 | Implemented: English purchase-chat parse for sharpness, protection, traps, feather falling, heal pool, and forge on a draggable HUD. ViaVersion chat remains required. |
| ViewClip | Utility | 0 | Implemented: third-person camera collision distance is bypassed while enabled. ViaVersion validation remains required. |

Core registers only the 79 original names. Sample extensions such as TNT-Timer or ESP are not built in.

## Remaining acceptance work

- Visually compare every category/control state with the original, including text baselines, gradient/texture sampling, bright accent text and nested background endings.
- Exercise the live screen for all mouse buttons, dropdown interception, focus changes, bind clearing, long lists, resolution/scale changes and reopening/restarting.
- Finish editor provider coverage and exact previews/bounds for the original HUD workflows as those modules are behaviorally ported.
- Verify that each partial native behavior consumes its restored controls; unsupported settings are retained, not advertised as working features.
- Validate extension reload/resource/config compatibility in a running client. Original reload is `/reload`, not `/meowtils reload`.
- Keep this ledger current; do not claim full GUI or 2.0.1 behavior parity until those checks are complete.

## Build and install

`JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home ./gradlew test build` is the required verification command. `./build.sh` runs it using JDK 25 and installs the main jar in the Prism `idk meowtils test` instance, with recoverable jar backups outside `mods`. Restart the instance to load a new build.
