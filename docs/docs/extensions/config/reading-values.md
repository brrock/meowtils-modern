# **Reading Values**

You can read `@Config` values from anywhere, as long as they are `public`. This includes modules that exist in **Meowtils** or other loaded extensions.

!!! Tip

    This is useful if you need to check if another feature is enabled, even if it doesn't exist in your extension.

## Accessing the value

``` java title="Reading value"
Module.get(ExampleModule.class).exampleSetting
```

This has to point to the module class you are trying to read a value from, and then the field name of that value.

!!! Example

    ``` java
    if (Module.get(Settings.class).smoothFont) {
        Meowtils.addMessage("Smooth font is currently enabled");
    }
    ```