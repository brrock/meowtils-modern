# **Bind Component**

---

**For:** `int values`

This component will allow you to bind a key.

![Bind](../../../assets/bind.png)

!!! tip "Recommended Usage"

    This should be used for when you want to assign a key to an int representing a key code.
    
---

``` java
bind(String name, String config)

// Example
bind("Example bind", "exampleKey");
```

### Config

The name of the `@Config` field.

!!! Example

    ``` java
    @Config public int exampleKey = 0;
    ```