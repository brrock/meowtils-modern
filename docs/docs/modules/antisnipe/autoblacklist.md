---
tags:
  - Legit
---

# **AutoBlacklist**

#### Automatically adds players to the blacklist for selected events.

---

## Show notifications

Will notify you when a player automatically gets added to the blacklist if they weren't already blacklisted.

## For flags

!!! Note

    This requires you to also use the [AntiCheat Module](../antisnipe/anticheat.md) & [SniperWarning Module](../antisnipe/sniperwarning.md).

### Enabled

Will enable auto-blacklisting if a player flags the anticheat.

### When flags

- AutoBlock
- NoSlow
- Killaura
- Legit Scaffold

## For reports

### Enabled

Will enable auto-blacklisting for reports.

### When /report

If you report someone using a `/report` command, it will add them to the blacklist with selected reasons, if none are selected it defaults to `cheating`.

!!! Info

    Most servers use the `/report` command, it will work on all of these.

### When /wdr

If you report someone using `/wdr` or `/watchdogreport` it will add them to the blacklist with selected reasons, if none are selected it defaults to `cheating`.

!!! Info

    These commands are only used on Hypixel.

## For snipers

### Enabled

Will enable auto-blacklisting for players that trigger the **SniperWarning** module.

### For Name

Will blacklist when someone is determined to have a potential sniper name.

### For Gear

Will blacklist when someone has known sniper gear.

### For Stats

Will blacklist when someone has suspicious stats.