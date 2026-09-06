# **Team Utility**

---

## **Bot Check**

```java
// Returns true if they are a bot
TeamUtil.isBot(EntityPlayer player)
```

This checks if a player is a bot, also known as AntiBot. This uses the **Meowtils** bot check which is configured in [Teams Module](../../modules/meowtils/teams.md).

## Existing Modes

!!! Warning

    While this generally works on most servers, it can always fail if a server changes how they handle things or if it is configured incorrectly.

### Hypixel

This checks UUID version to determine if a player is a bot or not, generally on Hypixel any NPC's will have a version 2 UUID.

### Universal

This attempts to determine if a player is a bot based on if they show up in the tablist, and if so for how long. This should generally work on most servers.

### Dynamic

This just changes between **Hypixel** & **Universal** modes depending on where they are known to work.

## **Team Check**

``` java
// Returns true if they are on your team
TeamUtil.isTeam(EntityPlayer player)
```

This is used to determine if a player is on the same team as you or not.

!!! Warning

    This attemps several different methods to check if a player is your teammate or not, such as depending on name color or armor color. However it may not always be accurate which depends on server & gamemode.

    If this is the case you should implement your own team check designed for that specific scenario.

## **Ignore Friends**

``` java
// Returns true if a player is friended & ignore friends is enabled in Teams module
TeamUtil.ignoreFriends(String uuidOrName)
```

This is used to ignore players you have friended on **Meowtils**, using the [Friends List](../../other/lists/friends.md). It will also be required for ignore friends to be enabled in the [Teams Module](../../modules/meowtils/teams.md).