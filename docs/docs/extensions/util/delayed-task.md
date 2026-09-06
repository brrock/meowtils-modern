# **Delayed Task Utility**

---

``` java
DelayedTask(Runnable task, int ticks)
```

This will allow you to delay something to run for set ticks, after the ticks have passed it will now run.

!!! Example

    ``` java
    new DelayedTask(() -> {
        Meowtils.addMessage("Delayed message");
    }, 40);
    ```