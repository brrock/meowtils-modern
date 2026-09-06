# **Toggle Component**

---

**For:** `boolean values`

This component will display a button and text, it allows you to set settings to either be true or false.

![Enabled](../../../assets/toggle-enabled.png)

![Disabled](../../../assets/toggle-disabled.png)

!!! tip "Recommended Usage"

    This should be used for main settings that can be toggled.
    
---

``` java
toggle(String name, String config)

// Example
toggle("Example setting", "exampleSetting");
```

### Name

The name, should be relevant to what it changes.

### Config

The name of the `@Config` field.

!!! Example

    ``` java
    @Config public boolean exampleSetting = false;
    ```