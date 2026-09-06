# **Saving Values**

This saves a value to the **Meowtils** config file, commonly used for sub-components but you can save any value if wanted.

## Requirements

1. This has to be located in a module class.

2. Each variable will need the `@Config` annotation.

3. You should generally keep it `public`.

!!! Example

    ``` java
    @Config public String mode = "Normal";
    ```

## Save to config

When used in module sub-components, it will automatically be saved to config if updated.

If you are manually setting values you need to save it:

``` java title="Manual save"
ConfigManager.save();
```

More about setting config values can be found [**HERE**](../config/modifying-values.md).