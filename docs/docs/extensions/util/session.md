# **Session Utility**

There are a few existing ways to check if the player is currently on a specific server or in a specific game.

!!! Note

    More modes will be added in the future. If you want you can post suggestions of what mode checks you would like to see added [**HERE**](../../support/suggest-feature.md).

!!! Info

    These are all based on scoreboard and may not work on every server.

``` title="Compact List"
Server:
UNIVERSAL
HYPIXEL
HYPIXEL_REPLAY
MINEPLEX

Bedwars:
ALL
LOBBY
PRE_GAME
PRACTICE
GAME
SOLOS
DOUBLES
THREES
FOURS
ONE_BLOCK
FOUR_FOUR

Skywars:
ALL
LOBBY
GAME
MINI

Duels:
ALL
LOBBY
BEDWARS

Mega Walls:
ALL
LOBBY
GAME

Murder Mystery:
ALL
```

---

## **Server**

Checks which server you are currently on, or server-specific areas such as Hypixel replay.

---

### _Universal_

``` java
// Returns true if active
Server.UNIVERSAL.isActive()

// Returns true if not active
Server.UNIVERSAL.isNotActive();
```

This is meant to be limited to minigame servers that are similar to Hypixel.

_Currently this only applies to `BlocksMC` & `Fakepixel`._

### _Hypixel_

``` java
// Returns true if active
Server.HYPIXEL.isActive()

// Returns true if not active
Server.HYPIXEL.isNotActive();
```

Limited to Hypixel server.

### _Hypixel Replay_

``` java
// Returns true if active
Server.HYPIXEL_REPLAY.isActive()

// Returns true if not active
Server.HYPIXEL_REPLAY.isNotActive();
```

Limited to Hypixel replay.

### _Mineplex_

``` java
// Returns true if active
Server.MINEPLEX.isActive()

// Returns true if not active
Server.MINEPLEX.isNotActive();
```

Limited to Mineplex server.

---

## **Bedwars**

Checks if you are currently in Bedwars and which mode.

!!! Info

    These checks are mostly made for Hypixel and may not work properly on other servers.

---

### _All_

``` java
// Returns true if active
Bedwars.ALL.isActive()

// Returns true if not active
Bedwars.ALL.isNotActive();
```

Limited to all types of Bedwars modes, both lobby and in-game.

!!! Tip

    It is recommended that you use this one if you want multi-server support, as it only requires the scoreboard title to contain `bedwars` or `bed wars`.

### _Lobby_

``` java
// Returns true if active
Bedwars.LOBBY.isActive()

// Returns true if not active
Bedwars.LOBBY.isNotActive();
```

Limited to Bedwars lobby.

### _Pre Game_

``` java
// Returns true if active
Bedwars.PRE_GAME.isActive()

// Returns true if not active
Bedwars.PRE_GAME.isNotActive();
```

Limited to Bedwars pre-game.

### _Practice_

``` java
// Returns true if active
Bedwars.PRACTICE.isActive()

// Returns true if not active
Bedwars.PRACTICE.isNotActive();
```

Limited to Bedwars practice.

### _Game_

``` java
// Returns true if active
Bedwars.GAME.isActive()

// Returns true if not active
Bedwars.GAME.isNotActive();
```

Limited to Bedwars in-game only, any mode.

### _Solos_

``` java
// Returns true if active
Bedwars.SOLOS.isActive()

// Returns true if not active
Bedwars.SOLOS.isNotActive();
```

Limited to Bedwars solos (1v1 x 4).

### _Doubles_

``` java
// Returns true if active
Bedwars.DOUBLES.isActive()

// Returns true if not active
Bedwars.DOUBLES.isNotActive();
```

Limited to Bedwars doubles (2v2 x 4).

### _Threes_

``` java
// Returns true if active
Bedwars.THREES.isActive()

// Returns true if not active
Bedwars.THREES.isNotActive();
```

Limited to Bedwars threes (3v3v3).

### _Fours_

``` java
// Returns true if active
Bedwars.FOURS.isActive()

// Returns true if not active
Bedwars.FOURS.isNotActive();
```

Limited to Bedwars fours (4v4v4v4).

### _Four vs Four_

``` java
// Returns true if active
Bedwars.FOUR_FOUR.isActive()

// Returns true if not active
Bedwars.FOUR_FOUR.isNotActive();
```

Limited to Bedwars four vs four (4v4).

### _One Block_

``` java
// Returns true if active
Bedwars.ONE_BLOCK.isActive()

// Returns true if not active
Bedwars.ONE_BLOCK.isNotActive();
```

Limited to Bedwars one-block.

---

## **Skywars**

Checks if you are currently in Skywars and which mode.

!!! Info

    These checks are mostly made for Hypixel and may not work properly on other servers.

--- 

### _All_

``` java
// Returns true if active
Skywars.ALL.isActive()

// Returns true if not active
Skywars.ALL.isNotActive();
```

Limited to all types of Skywars modes, both lobby and in-game.

!!! Tip

    It is recommended that you use this one if you want multi-server support, as it only requires the scoreboard title to contain `skywars` or `sky wars`.

### _Lobby_

``` java
// Returns true if active
Skywars.LOBBY.isActive()

// Returns true if not active
Skywars.LOBBY.isNotActive();
```

Limited to Skywars lobby.

### _Game_

``` java
// Returns true if active
Skywars.GAME.isActive()

// Returns true if not active
Skywars.GAME.isNotActive();
```

Limited to Skywars in-game only, any mode.

### _Mini_

``` java
// Returns true if active
Skywars.MINI.isActive()

// Returns true if not active
Skywars.MINI.isNotActive();
```

Limited to Skywars mini mode.

---

## **Duels**

Checks if you are currently in Duels and which mode.

!!! Info

    These checks are mostly made for Hypixel and may not work properly on other servers.

---

### _All_

``` java
// Returns true if active
Duels.ALL.isActive()

// Returns true if not active
Duels.ALL.isNotActive();
```

Limited to all types of Duels modes, both lobby and in-game.

!!! Tip

    It is recommended that you use this one if you want multi-server support, as it only requires the scoreboard title to contain `duels`.

### _Lobby_

``` java
// Returns true if active
Duels.LOBBY.isActive()

// Returns true if not active
Duels.LOBBY.isNotActive();
```

Limited to Duels lobby.

### _Bedwars_

``` java
// Returns true if active
Duels.BEDWARS.isActive()

// Returns true if not active
Duels.BEDWARS.isNotActive();
```

Limited to Duels bedwars mode.

---

## **Mega Walls**

Checks if you are currently in Mega Walls and which mode.

!!! Info

    These checks are mostly made for Hypixel and may not work properly on other servers.

--- 

### _All_

``` java
// Returns true if active
MegaWalls.ALL.isActive()

// Returns true if not active
MegaWalls.ALL.isNotActive();
```

Limited to all types of Mega Walls modes, both lobby and in-game.

!!! Tip

    It is recommended that you use this one if you want multi-server support, as it only requires the scoreboard title to contain `mega walls`.

### _Lobby_

``` java
// Returns true if active
MegaWalls.LOBBY.isActive()

// Returns true if not active
MegaWalls.LOBBY.isNotActive();
```

Limited to Mega Walls lobby.

### _Game_

``` java
// Returns true if active
MegaWalls.GAME.isActive()

// Returns true if not active
MegaWalls.GAME.isNotActive();
```

Limited to Mega Walls in-game only, any mode.

---

## **Murder Mystery**

Checks if you are currently in Murder Mystery.

!!! Info

    These checks are mostly made for Hypixel and may not work properly on other servers.

---

### _All_

``` java
// Returns true if active
MurderMystery.ALL.isActive()

// Returns true if not active
MurderMystery.ALL.isNotActive();
```

Limited to all types of Murder Mystery modes, both lobby and in-game.

!!! Tip

    It is recommended that you use this one if you want multi-server support, as it only requires the scoreboard title to contain `murder mystery`.