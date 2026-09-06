# **Color Utility**

---

## RGB int

``` java
ColorUtil.rgb(int red, int green, int blue)
```

Returns bit shifted RGB int.

## RGBA int

``` java
ColorUtil.rgba(int red, int green, int blue, int alpha)
```

Returns bit shifted RGB int with alpha.

## Get Color

``` java
ColorUtil.getColor(int red, int green, int blue)
```

Returns **java.awt.Color**.

``` java
ColorUtil.getColor(int red, int green, int blue, int alpha)
```

Returns **java.awt.Color** with alpha.

!!! Tip

    If updated a lot, you should always use this since the color is cached in order to prevent excessive allocations.

## RGB from EnumChatFormatting

``` java
ColorUtil.getRGBFromFormatting(EnumChatFormatting color)
```

Converts **EnumChatFormatting**, returns RGB int.

## Color from EnumChatFormatting

``` java
ColorUtil.getColorFromFormatting(EnumChatFormatting color)
```

Converts **EnumChatFormatting**, returns **java.awt.Color**.

## EnumChatFormatting from String

``` java
ColorUtil.getColorFromString(String colorName)
```

Returns **EnumChatFormatting** from string. The string should not contain any spaces or special symbols & must contain a valid color.

``` title="Valid Colors"
black
dark_blue
dark_green
dark_aqua
dark_red
dark_purple
gold
gray
dark_gray
blue
green
aqua
red
light_purple
yellow
white
```

## EnumChatFormatting from code

``` java
ColorUtil.getColorFromCode(char code)
```

Returns **EnumChatFormatting** from a Minecraft color code. Must be a valid code.

``` title="Valid Codes"
0
1
2
3
4
5
6
7
8
9
a
b
c
d
e
f
```

## Unformatted text

``` java
ColorUtil.unformattedText(String text)
```

Returns the string without any formatting codes.

## Convert formatting

``` java
ColorUtil.convertFormatting(String text)
```

Returns the string with converted Minecraft formatting codes, converts `&<code>` to `§<code>`. Will not convert `&` without a valid code.

!!! Example

    **Converts to gold text:**   
    &6Meow → §6Meow

    **Converts while preserving:**   
    &6Meow & &cMeow → §6Meow & §cMeow

## Convert opacity

``` java
ColorUtil.convertOpacity(float opacity)
```

Converts opacity `0-100%` to `0-255` alpha.