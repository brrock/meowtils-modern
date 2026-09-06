# **Scoreboard Utility**

## Get scoreboard

``` java
Scoreboard ScoreboardUtil.getScoreboard()
```

Returns `Scoreboard` if not null.

## Get sidebar

``` java
ScoreObjective ScoreboardUtil.getSidebar()
```

Returns `ScoreObjective` if not null.

## Get sidebar title

``` java
String ScoreboardUtil.getSidebarTitle()
```

Returns the full title of sidebar.

## Get sidebar lines

``` java
List<String> ScoreboardUtil.getSidebarLines()
```

Returns all lines of the sidebar.

## Title contains

``` java
boolean ScoreboardUtil.titleContains(String text)
```

Checks if the sidebar title contains specified text.

## Line contains

``` java
boolean ScoreboardUtil.lineContains(String text)
```

Checks if the sidebar contains specificed text on any line.