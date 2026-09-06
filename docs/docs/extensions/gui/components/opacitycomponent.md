# **Opacity Component**

---

**For:** `float values`

This component will display opacity from `0-100%`.

![Opacity-Slider](../../../assets/opacity-slider.png)

!!! tip "Recommended Usage"

    This should be used for modules where you can adjust opacity.
    
---

``` java
opacity(String name, String config)

// Example
opacity("Text opacity", "opacity");
```

### Config

The name of the `@Config` field.

!!! Example

    ``` java
    @Config public float opacity = 50;
    ```

!!! Tip

    This can be used with [Convert Opacity](../../utility/color.md) for ease of use.