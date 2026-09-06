# **Notifications**

These display a small toast notification in bottom-right corner.

``` java
// Normal notification, with default text scale
NotificationManager.show(String title, String message, Type type, long time)

// Custom text scale
NotificationManager.show(String title, float titleScale, String message, float messageScale, Type type, long time)
```

!!! Note

    This will not automatically adjust text for you, you will have to make sure your text isn't too long so it goes off-screen, etc.

    If setting custom scale; keep it within reasonable scale or else it will also not look correct.

    **Default title scale:** `7`

    **Default message scale:** `5`

## Types

Determines the type of notification, this only affects what icon is used and the color of the icon + title.

### Info

``` java
NotificationManager.Type.INFO
```

![Info-notification](../../assets/info-notification.png)

### Alert

``` java
NotificationManager.Type.ALERT
```

![Alert-notification](../../assets/alert-notification.png)

### Warning

``` java
NotificationManager.Type.WARNING
```

![Warning-notification](../../assets/warning-notification.png)

## Time

The time is in milliseconds and determine how long the notification will be visible for.

## Example

``` java
NotificationManager.show("Player Warning", "Player is invisible!", NotificationManager.Type.ALERT, 1500);
```