# **Blacklist**

The blacklist allows you to add players for any reason to a list that keeps track of them. This is your own personal blacklist and is stored in your game directory with other mod files.

It stores their name, timestamp of when they were blacklisted, and then what reasons they are blacklisted for.

## Usage

**Blacklisting a player:**

`/blacklist | /bl <player> <reasons>` - Add to blacklist.

_The reasons should be separated by a space, with the exception of "legit scaffold" which will be treated as one reason. If no reason is provided while blacklisting it will fall back to "cheating" as a generic reason._

`/unblacklist | /ubl <player\>` - Remove from blacklist.

`/blacklist info | /bl info <player\>` - Shows blacklist info.

!!! Example

    /bl tatp autoblock killaura fakelag

## Icons

Blacklisted players will have an icon in tablist and their nametag, depending on how you configure the [Icons Module](../../../modules/meowtils/icons/).

The blacklist icon color depends on what reasons they are blacklisted for, this is based on priority which means the color will represent the highest severity reason they are blacklisted for.

<span style="color: #aa0000">⚠</span> **= Highest severity blatant cheat reason**

<span style="color: #ffaa00">⚠</span> **= Medium severity closet cheat reason**

<span style="color: #ff55ff">⚠</span> **= Blacklisted & safelisted at the same time**

<span style="color: #00aa00">⚠</span> **= Any other custom reason**

## Alerts

You can enable alerts so if a player that is blacklisted shows up in your world, it will send you a warning.

This can be enabled in the [Notifications Module](../../../modules/meowtils/notifications/).

## Reasons

These are the pre-defined reasons provided, and their color will follow the severity as shown above. If you are blacklisting for a reason that is not pre-defined it will instead show up as green.

``` title="Blatant Reasons"
bednuker, 
fly, 
keepsprint, 
killaura, 
nofall, 
noslow, 
bhop, 
antifireball, 
safewalk, 
blink, 
autoblock, 
blatant, 
strafe, 
snipe, 
sniper, 
sniping
```

``` title="Closet Reasons"
cheating, 
aimassist, 
autoclicker, 
reach, 
velocity, 
esp, 
fakelag, 
legit 
scaffold, 
fastplace, 
closet, 
scaffold
```