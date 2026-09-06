# **SendPacketEvent**

---

This fires each time your client sends a packet to the server.

**Cancellable =** `true`

!!! Warning

    Cancelling packets is not safe and can lead to non-vanilla client behaviour that may flag anticheats. Only cancel packets when you know what you are doing.

## Parameter

`SendPacketEvent`

## Methods

### Packet

``` java
Packet<?> getPacket()
```

Returns current `Packet`.

## Example

``` java
/*
Cancels item release packets, also known as NoItemRelease.
*/
@EventTarget
public void onSendPacket(SendPacketEvent event) {
    if (event.getPacket() instanceof C07PacketPlayerDigging) {
        final C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.getPacket();

        if (packet.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
            event.setCancelled(true);
        }
    }
}
```