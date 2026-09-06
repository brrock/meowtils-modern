---
tags:
  - Legit
---

# **PartyNotifier**

#### This sends a message in party chat if you are in a party, for selected features.

!!! Note

    In order for an alert message to be sent, the target module itself has to be enabled. **PartyNotifier** is simply a way to enable alerts for party chat, for targeted modules.

!!! Tip

    You can edit the messages, use the arguments starting with `#` for each message in order to replace the argument with specified type of information.

---

## Show prefix

If enabled this will show the **Meowtils** prefix in front of messages that are sent, which is `Meow »`.

## Reset messages

Resets all messages back to default.

## AntiCheat notify

### Enabled

This sends a message for each [AntiCheat](../antisnipe/anticheat.md) alert.

### Message

`#player` - The player that flagged.

`#check` - The anticheat check name.

## Denicker notify

### Enabled

This sends a message each time someone is denicked, from [Denicker](../hypixel/denicker.md).

### Message

`#player` - The player that is nicked (their nickname).

`#denicked` - The real username, if they were successfully denicked.

## Urchin notify

### Enabled

This sends a message each time someone is alerted for being blacklisted in **Urchin**.

### Message

`#player` - The player that is blacklisted.

`#tag` - The tag they are blacklisted for.

## ArmorAlerts notify

### Enabled

This sends a message for each [ArmorAlerts](../bedwars/armoralerts.md) alert.

### Message

`#player` - The player it alerts for.

`#armor` - The armor type they upgraded to.

## BedTracker notify

### Enabled

This sends an alert every time [BedTracker](../bedwars/bedtracker.md) alerts.

### Message

`#player` - The player it alerts for.

`#distance` - The distance away from your bed they are.

`#warning` - The warning icon.

## ConsumeAlerts notify

### Enabled

This sends a message for each [ConsumeAlerts](../bedwars/consumealerts.md) alert.

### Message

`#player` - The player it alerts for.

`#item` - The item they consumed.

## ItemAlerts notify

### Enabled

This sends a message for each [ItemAlerts](../bedwars/itemalerts.md) alert.

### Message

`#player` - The player it alerts for.

`#item` - The item they are holding.

## UpgradeAlerts notify

### Enabled

This sends a message for each [UpgradeAlerts](../bedwars/upgradealerts.md) alert.

### Message

`#team` - The team that got an upgrade.

`#upgrade` - The upgrade they got.

## PartyDetector notify

### Enabled

This sends a message for each [PartyDetector](../antisnipe/partydetector.md) alert.

### Enabled

`#amount` - The amount of players that joined at once.