# **Check Component**

---

**For:** `boolean values`

This component will display a box and text, it allows you to set settings to either be true or false. This is functionally identical to the [Toggle Component](../components/togglecomponent.md).

![Enabled](../../../assets/check-enabled.png)

![Disabled](../../../assets/check-disabled.png)

!!! tip "Recommended Usage"

    This should be used for toggling lists, for example which items are selected.

---

``` java
check(String name, String config)

// Example
check("Example setting", "exampleSetting");
```

### Name

The name, should be relevant to what it changes.

### Config

The name of the `@Config` field.

!!! Example

    ``` java
    @Config public boolean exampleSetting = false;
    ```