# **Brightness Component**

---

**For:** `int values`

This component will display a slider for setting brightness.

![Brightness-Slider](../../../assets/brightness-slider.png)

!!! tip "Recommended Usage"

    This should be used when you want to adjust an `RGB` value.
    
---

``` java
brightness(@Nonnull ColorLink link)

// Example
brightness(color);
```

### Color linker

When using this component you should use `linkColor`, which lets you link several different components together. This would be used to link **color** with **saturation** and **brightness**.

``` java
linkColor(String redConfig, String greenConfig, String blueConfig)
```

!!! Example

    **Linking one color:**

    ``` java
    linkColor("red", "green", "blue");
    ```

    **Linking several individual colors:**

    ``` java
    ColorLink color = linkColor("red", "green", "blue");
    ColorLink color2 = linkColor("red2", "green2", "blue2");
    ```

They require `@Config` fields for `red`, `green`, `blue`.

!!! example "Config Fields"

    ``` java
    @Config public int red = 255;
    @Config public int green = 255;
    @Config public int blue = 255;
    ```