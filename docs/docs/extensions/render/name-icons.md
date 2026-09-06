# **Render Name Icons**

This lets you add icons or text to player nametags, which will show up in both tablist & nametags depending on how you limit it.

!!! Bug

    If you are using Lunar Client nametag icons will only be displayed in tablist.

---

## Interface

``` java
import wtf.tatp.meowtils.manager.icons.IconProvider;
```

### Overrides

!!! Info

    You can skip overriding these if you have no intentions of modifying the prefix/suffix. Additionally, tablist or nametag booleans will only return true if they are for the tablist/nametag. Check the example below.

``` java
getPrefix(GameProfile profile, boolean tablist, boolean nametag)
```

String, used for setting prefix (what comes before) the player name.

``` java
getSuffix(GameProfile profile, boolean tablist, boolean nametag)
```

String, used for setting suffix (what comes after) the player name.

## Registering

You will need to register your implementation at init.

!!! Example

    ``` java
    IconManager.register(new ExampleIcon());
    ```

## Example

``` java
/*
This displays a prefix and suffix for yourself that says if it is currently being rendered in tablist or nametag.
*/
public class ExampleIcon implements IconProvider {

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        if (profile.getId() != Minecraft.getMinecraft().thePlayer.getUniqueID()) return "";
        if (tablist) {
            return "Tablist ";
        }
        return "Nametag ";
    }

    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        if (profile.getId() != Minecraft.getMinecraft().thePlayer.getUniqueID()) return "";
        if (tablist) {
            return " Tablist";
        }
        return " Nametag";
    }
}
```