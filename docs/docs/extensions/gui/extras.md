# **Extra Module Utility**

## Tooltip

You can add a tooltip, this will display your tooltip while hovering the module. It is useful to use for a brief explanation of what the module does.

**You need to register this the same way components are registered.**

!!! Example

    ``` java
    tooltip("This is an example module.");
    ```

## Set state

You can change the state of the current module. Generally you would only use this to disable your module for whatever reason.

!!! Example

    ``` java
    setState(final boolean enabled)
    ```

