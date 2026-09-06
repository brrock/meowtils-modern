# **Extra Utility**

## Loaded Class

``` java
Util.isClassLoaded(String path)
```

Checks if a java class is loaded, this is useful if you need to detect another mod for example.

## Open Folder

``` java
Util.openFolder(File dir, String id)
```

Will open the selected folder if supported.

`File` - The path.

`id` - Name used for notifications.

## Format Timestamp

``` java
Util.formatTimestamp(long ms)
```

Uses unix timestamp, returns a string with the formatted time.

**Format:** `yyyy-MM-dd HH:mm`

## Format Time

``` java
Util.formatTime(int seconds)
```

Returns a string with seconds & minutes formatted, including `s` & `m` as suffix.

## Parse Int

``` java
Util.parseIntFromString(String string, int start)
```

Parses ints from a string by checking everything after your starting point, breaks at the end of the value.

!!! Example

    If the string we want to parse a value from is `Kills: 9` we would put the start at `7`.