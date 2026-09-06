---
tags:
  - Legit
---

# **Teams**

#### Makes modules ignore teammates and bots.

---

## Ignore bots

Attempts to ignore players that might be bots, also known as AntiBot.

!!! Question "Bots"

    This is usually used for modules that check players, such as alerts and stats. This will exclude potential anticheat bots and NPC's from real players.

### Hypixel

Made for Hypixel, which ignores players based on UUID version. May not work reliably on other servers.

### Universal

Ignores players that do not show up in tablist long enough, which should work on most servers.

### Dynamic

Changes between Hypixel & Universal modes depending on which server you're currently on.

### None

Will not ignore any players even if they are bots.

## Ignore team

Will attempt to ignore players that are on the same team as you.

!!! Question "Team"

    This is usually used for modules where you don't want them to trigger for your own teammates, this checks a variety of information available to determine if they are a teammate or not. It may not be 100% accurate.

## Ignore friends

Ignores players that are in your **Meowtils** friends list.

!!! Question "Friends"

    This is usually used by modules where you may not want specific players to trigger, similar to the team check except you can manually exclude which players that should be ignored by adding them to your friends list.