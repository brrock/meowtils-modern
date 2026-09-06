# **HUD Editor**

---

You can add your own HUD Elements to the **Meowtils** HUD Editor, this would allow you to drag & drop them to change positions.

---

!!! Note

    This requires you to add X & Y position `@Config` fields.

    [Saving Values](../../config/saving-values.md)

``` java
// Single element
@Override
public List<HudEntry> hudEditor() {
    return Collections.singletonList(
            new HudEntry("Health", this, "healthPosX", "healthPosY",
            () -> GuiUtil.getHudBounds("20V", 1, healthScale)));
}

// Multiple elements
@Override
public List<HudEntry> hudEditor() {
    return Arrays.asList(
            new HudEntry("Health", this, "healthPosX", "healthPosY",
                    () -> GuiUtil.getHudBounds("20V", 1, healthScale)),
            new HudEntry("Bow Damage", this, "bowPosX", "bowPosY",
                    () -> GuiUtil.getHudBounds(hideSuffix ? "20 " : "20 HP ", 1, bowScale))
    );
}
```

## HudEntry

``` java
public HudEntry(@Nullable String name, Module owner, String xPosConfigField, String yPosConfigField, Supplier<int[]> bounds)
```

### Name

This will be displayed while hovering the element in the editor, if set to `null` it will just show the module name instead.

!!! Tip

    You should use this when adding several HUD Elements in a single module, to distinguish which is which.

### Owner

The current owner of this element, should be set as `this`.

### X Pos

The config field name for `X Pos`.

### Y Pos

The config field name for `Y Pos`.

### Bounds

The element bounds, for this you should use **getHudBounds** shown below.

## Hud Bounds

!!! Tip

    There is really no right or wrong with this, you will just have to test and see what fits the best. This is just for controlling the bounds when hovering & dragging your element, when in HUD Editor.

``` java
int[] GuiUtil.getHudBounds(String longestText, int lineCount, float scale)
```

### Longest text

**Horizontal bounds.**

This would be the longest that the text in your element might get.

### Line count

**Vertical bounds.**

The number of lines your element might be.

### Scale

Current scale of your element.

## Rendering

When you render your element you should also check if you are in the editor, and if so also render your element.

``` java
// Returns true if you're currently in editor
GuiUtil.inEditor()
```