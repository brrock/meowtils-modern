# **Modifying Values**

You can modify `@Config` values from anywhere, as long as they are `public`. This includes modules that exist in **Meowtils** or other loaded extensions.

!!! Warning

    You should avoid doing this unless you know what it leads to, as in some cases it may lead to weird bugs or even crashes.

## Modifying the value

``` java title="Modifying value"
Module.get(ExampleModule.class).exampleSetting = false;
```

This has to point to the module class you are trying to modify a value for, and then the field name of that value.

You will also need to manually save this to config, which can be read more about [**HERE**](../config/saving-values.md).

!!! Example

    ``` java
    Settings s = Module.get(Settings.class);
    if (s.smoothFont) {
        s.smoothFont = false;
        ConfigManager.save();
        Meowtils.addMessage("Smooth font has been disabled!");
    }
    ```