---
tags:
  - Legit
---

# **Stats**

#### Display game stats for players.

---

## API

### Hypixel

Uses the Hypixel API to fetch stats.

!!! info "Hypixel API"

    In order to use the Hypixel API you need a key.

    **How to obtain an API key:**

    1. Go to [Hypixel Developer Dashboard](https://developer.hypixel.net/).

    2. Sign in with your Hypixel forums account.

    3. Click `CREATE API KEY`

    4. In-game, add your key with `/meowapi <key>`

    _Normal keys don't last forever and will have to be regenerated. You may also need a forums account that is older than a day._


### Abyss

Uses the [Abyss Overlay](https://github.com/Chit132/abyss-overlay) API for fetching stats, does not require any API key.

!!! Warning

    We simply provide the alternative for you to use this, but they likely do not intend for their API to be used this way, and if abused they might ban you.

    As long as it is used normally (loading stats ingame) you shouldn't run into any issues as this is what their overlay already does.



## Clear cache

To prevent rate limits, stats are cached. This allows you to clear that cache.

!!! Note

    Only do this when you know what it leads to, as clearing cache could lead to further rate limits.

## Cache duration

Lets you set how many minutes stats will be cached for.

## Fetch cooldown

This is the delay between requests, a higher delay may prevent issues with rate limits but it would also mean it will take more time until all stats have loaded.

## Display mode

**Modes:** _Full, Compact, Lowercase_

Changes how stats look in tab.

## Display

### Tablist

Will show stats in tab.

### Nametags

Will show limited stats in player nametags, similar to how levelhead would work in other mods.

### Chat

Shows stats for all players in chat, this requires you to use [AutoWho](../autowho) or run `/who` manually.

## Chat options

### Auto-check certain players

This automatically fetches stats and sends them in chat for some players.

**Applies to:**

- Players in party, when running `/party list`.

- Players that mention your name in public chat.

- Players that talk in Bedwars pre-game.

- Denicked players.

### Use team colors

Colors the names for chat stats based on the team they're on, rather than their rank.

## Urchin

!!! Info "Urchin API"

    In order to use Urchin you need an API key.

    **How to obtain an API key:**

    1. Join [Urchin Discord](https://discord.gg/urchin)

    2. Verify with your username, this assumes you have linked your discord to your username on Hypixel.

    3. Run /info then /dashboard to generate your API key with their coral discord bot.

### Check Urchin API

Will check if a player is in the Urchin blacklist.

### Ignore self

Ignore checking & rendering Urchin icon for yourself.

### Show name icons

Will show Urchin icons in nametag & tablist for players that are in the Urchin blacklist.

**Icons:**

<span style="color: #aa0000">✹</span> = Blatant Cheater

<span style="color: #aa00aa">✹</span> = Confirmed Cheater

<span style="color: #ffff55">✴</span> = Closet Cheater

<span style="color: #ff5555">✹</span> = Sniper

<span style="color: #aaaaaa">✹</span> = Info

<span style="color: #555555">✹</span> = Account

<span style="color: #ffff55">ⓘ</span> = Caution

### Show in chat

Will send a message in chat if a player is blacklisted on Urchin, requires [AutoWho](../autowho) or manually running `/who`.

This only applies to games that support the `/who` command.

### API key

Your Urchin API key.

## Bedwars

### Enabled

If bedwars stats should show for players.

### Level

Their bedwars level (star).

### Final kills

The amount of final kills they have.

### FKDR

The final-kill-death ratio of a player.  
_Calculation: final kills / final deaths._

### WLR

The win-loss ratio of a player.  
_Calculation: wins / losses._

### Winstreak

The winstreak they have.

!!! Note

    This will only be displayed for players that actually have a winstreak visible, as players are able to hide winstreaks from the API.

### Clutch ratio

The clutch ratio of a player.  
_Calculation: 1 - final deaths / beds lost._

## Skywars

### Enabled

If skywars stats should show for players.

### Level

Their skywars level (star).

### Kills

The amount of kills they have.

### Wins

The amount of wins they have.

### KDR

The kill-death ratio of a player.  
_Calculation: kills / deaths._

### WLR

The win-loss ratio of a player.  
_Calculation: wins / losses._