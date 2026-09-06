# **Sound Utility**

## Play Sound

``` java
Util.playSound(Util.Sound sound, int volume)
```

There are a few pre-defined sounds you can use for convenience.

``` title="Sounds"
    PING
    PING_DEEP
    PING_MEDIUM
    LEVEL
    ANVIL
    MEOW
    ANVIL_BREAK
    ERROR
    ERROR_DEEP
    CRIT
    CLICK
```

!!! Note

    The volume should be between `0-100`, for most applications you should just put it at `100`.